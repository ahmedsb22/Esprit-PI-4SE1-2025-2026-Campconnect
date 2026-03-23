#!/bin/bash

# ============================================================
# SCRIPT DE TEST - Intégration des Rôles dans le JWT
# ============================================================

# Configuration
API_URL="http://localhost:8080"
ADMIN_EMAIL="admin@campconnect.tn"
ADMIN_PASSWORD="Admin@123456"
CAMPER_EMAIL="camper@campconnect.tn"
CAMPER_PASSWORD="Camper@123456"

echo "=========================================="
echo "🧪 TESTS JWT - INTÉGRATION DES RÔLES"
echo "=========================================="
echo ""

# Test 1: Login Admin
echo "📝 Test 1: Login Admin"
echo "POST $API_URL/api/auth/login"
ADMIN_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$ADMIN_EMAIL\",\"password\":\"$ADMIN_PASSWORD\"}")

echo "Response:"
echo "$ADMIN_RESPONSE" | jq '.'
ADMIN_TOKEN=$(echo "$ADMIN_RESPONSE" | jq -r '.token')
echo "✅ Token obtenu: ${ADMIN_TOKEN:0:30}..."
echo ""

# Test 2: Décoder le JWT pour voir les rôles
echo "📝 Test 2: Analyser le JWT"
echo "Payload du JWT Admin:"
echo "$ADMIN_TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.' || echo "Note: jq n'est pas disponible, installez-le pour voir les details"
echo ""

# Test 3: Login Camper
echo "📝 Test 3: Login Camper"
echo "POST $API_URL/api/auth/login"
CAMPER_RESPONSE=$(curl -s -X POST "$API_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$CAMPER_EMAIL\",\"password\":\"$CAMPER_PASSWORD\"}")

echo "Response:"
echo "$CAMPER_RESPONSE" | jq '.'
CAMPER_TOKEN=$(echo "$CAMPER_RESPONSE" | jq -r '.token')
echo "✅ Token obtenu: ${CAMPER_TOKEN:0:30}..."
echo ""

# Test 4: Utiliser le token Admin pour une requête protégée
echo "📝 Test 4: Requête protégée avec token Admin"
echo "GET $API_URL/api/auth/profile"
curl -s -X GET "$API_URL/api/auth/profile" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.'
echo ""

# Test 5: Tester un endpoint nécessitant un rôle spécifique
echo "📝 Test 5: Endpoint protégé par rôle"
echo "GET $API_URL/api/admin/users (nécessite rôle ADMIN)"
echo "Avec token Admin:"
curl -s -X GET "$API_URL/api/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" | jq '.[] | {id, email, firstName, roles}' | head -5
echo ""

echo "📝 Test 6: Même endpoint avec token Camper"
echo "GET $API_URL/api/users (nécessite rôle ADMIN)"
echo "Avec token Camper (devrait échouer):"
curl -s -X GET "$API_URL/api/users" \
  -H "Authorization: Bearer $CAMPER_TOKEN" -v 2>&1 | grep "< HTTP"
echo ""

# Test 7: Test sans token
echo "📝 Test 7: Requête sans token (devrait échouer)"
echo "GET $API_URL/api/auth/profile"
curl -s -X GET "$API_URL/api/auth/profile" | jq '.'
echo ""

echo "=========================================="
echo "✅ TESTS TERMINÉS"
echo "=========================================="
