# CampConnect — Entities & Associations

## Entity Overview

| Entity | Table | Description |
|--------|-------|-------------|
| `User` | `users` | Platform users: admins, campers, and site owners |
| `Role` | `roles` | Roles: ADMIN, CAMPER, OWNER |
| `CampingSite` | `camping_sites` | Camping sites listed by owners |
| `Equipment` | `equipment` | Rental equipment provided by owners |
| `Reservation` | `reservations` | A camper's booking for a camping site |
| `ReservationEquipment` | `reservation_equipment` | Equipment items ordered within a reservation |
| `Contract` | `contracts` | Signed agreement attached to a reservation |
| `Invoice` | `invoices` | Payment invoice for a reservation or equipment order |

---

## Entity Details & Fields

### User
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| email | String (unique) | Login email |
| password | String | Plain text for demo |
| firstName | String | First name |
| lastName | String | Last name |
| phone | String | Phone number |
| address | String | Physical address |
| profileImage | String | URL to profile photo |
| createdAt | Instant | Creation timestamp |
| updatedAt | Instant | Last update timestamp |
| roles | Set\<Role\> | ManyToMany via `user_roles` |

### Role
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| name | RoleName (ENUM) | ADMIN / CAMPER / OWNER |
| permissions | String | Comma-separated permissions |

### CampingSite
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| name | String | Site name |
| description | TEXT | Detailed description |
| location | String | City / region |
| address | String | Full address |
| pricePerNight | BigDecimal | Price in TND |
| capacity | Integer | Max guests |
| category | String | BEACH / MOUNTAIN / FOREST / DESERT / NATURE / GLAMPING |
| imageUrl | String | Cover image URL |
| hasWifi | Boolean | WiFi available |
| hasParking | Boolean | Parking available |
| hasRestrooms | Boolean | Restrooms available |
| hasShowers | Boolean | Showers available |
| hasElectricity | Boolean | Electricity available |
| hasPetFriendly | Boolean | Pets allowed |
| isActive | Boolean | Site is open for bookings |
| isVerified | Boolean | Approved by admin |
| rating | BigDecimal | Average rating (0–5) |
| reviewCount | Integer | Number of reviews |
| owner | User | ManyToOne → User (OWNER role) |
| reservations | Set\<Reservation\> | OneToMany → Reservation |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

### Equipment
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| name | String | Equipment name |
| description | TEXT | Detailed description |
| category | String | TENTS / SLEEPING_GEAR / COOKING / LIGHTING / BAGS / WATER_SPORTS / BIKES / FURNITURE / ELECTRONICS |
| pricePerDay | BigDecimal | Rental price per day (TND) |
| stockQuantity | Integer | Total units owned |
| availableQuantity | Integer | Units currently available |
| imageUrl | String | Cover image URL |
| specifications | String | Key specs (e.g., "6 persons, Waterproof") |
| isActive | Boolean | Available for rental |
| rating | BigDecimal | Average rating (0–5) |
| reviewCount | Integer | Number of reviews |
| provider | User | ManyToOne → User (OWNER role) |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

### Reservation
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| reservationNumber | String (unique) | e.g. RES-001 |
| checkInDate | LocalDate | Arrival date |
| checkOutDate | LocalDate | Departure date |
| numberOfGuests | Integer | Guest count |
| totalPrice | BigDecimal | Total amount in TND |
| status | ReservationStatus | PENDING / CONFIRMED / CANCELLED / CHECKED_IN / CHECKED_OUT / COMPLETED |
| specialRequests | TEXT | Guest notes |
| camper | User | ManyToOne → User (CAMPER role) |
| campingSite | CampingSite | ManyToOne → CampingSite |
| equipments | Set\<ReservationEquipment\> | OneToMany → ReservationEquipment |
| contract | Contract | OneToOne → Contract |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

### ReservationEquipment
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| quantity | Integer | Units rented |
| pricePerDay | BigDecimal | Price at time of order |
| subtotal | BigDecimal | quantity × pricePerDay × days |
| reservation | Reservation | ManyToOne → Reservation |
| equipment | Equipment | ManyToOne → Equipment |

### Contract
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| contractNumber | String (unique) | e.g. CTR-001 |
| terms | TEXT | Contract text |
| isSigned | Boolean | Signed by camper |
| signedAt | LocalDateTime | Signing timestamp |
| signatureUrl | String | URL to signature image |
| status | ContractStatus | DRAFT / PENDING / SIGNED / ACTIVE / COMPLETED / CANCELLED |
| reservation | Reservation | OneToOne → Reservation (unique FK) |
| createdAt | LocalDateTime | Creation timestamp |
| updatedAt | LocalDateTime | Last update timestamp |

### Invoice
| Field | Type | Description |
|-------|------|-------------|
| id | Long (PK) | Auto-generated |
| invoiceNumber | String (unique) | e.g. INV-001 |
| totalAmount | BigDecimal | Amount in TND |
| status | InvoiceStatus | DRAFT / SENT / PAID / CANCELLED |
| issuedAt | LocalDateTime | Issue date |
| notes | TEXT | Notes or payment instructions |
| reservation | Reservation | ManyToOne → Reservation (nullable) |
| equipmentOrder | ReservationEquipment | ManyToOne → ReservationEquipment (nullable) |

---

## Relationships Diagram

```
User ──────────────────────────── ManyToMany ──── Role
 │                                               (user_roles)
 │ (owner_id)
 ├──── OneToMany ──────────────── CampingSite
 │                                    │
 │ (provider_id)                      │ (camping_site_id)
 ├──── OneToMany ──────────────── Equipment
 │                                    │
 │ (camper_id)                        │
 └──── OneToMany ──────────────── Reservation ── ManyToOne ─► CampingSite
                                      │
                                      ├── OneToMany ─► ReservationEquipment ─► Equipment
                                      │                        │
                                      │                        └── ManyToOne ─► Invoice
                                      │
                                      ├── OneToOne  ─► Contract
                                      │
                                      └── ManyToOne ─► Invoice
```

---

## CRUD Operations by Entity

### CampingSite — `/api/sites`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/sites` | Get all camping sites |
| GET | `/api/sites/active` | Get active & verified sites (used by frontend) |
| GET | `/api/sites/{id}` | Get one site by ID |
| GET | `/api/sites/search?location=&category=&minPrice=&maxPrice=` | Search/filter sites |
| POST | `/api/sites` | Create a new camping site |
| PUT | `/api/sites/{id}` | Update a camping site |
| DELETE | `/api/sites/{id}` | Delete a camping site |
| PUT | `/api/sites/{id}/approve` | Admin approves a site (isVerified=true) |
| PUT | `/api/sites/{id}/reject` | Admin rejects a site (isVerified=false) |

### Equipment — `/api/equipment`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/equipment` | Get all equipment |
| GET | `/api/equipment/active` | Get active equipment |
| GET | `/api/equipment/{id}` | Get one item |
| POST | `/api/equipment` | Create equipment |
| PUT | `/api/equipment/{id}` | Update equipment |
| DELETE | `/api/equipment/{id}` | Delete equipment |

### Reservations — `/api/bookings`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/bookings` | Get all reservations |
| GET | `/api/bookings/{id}` | Get one reservation |
| POST | `/api/bookings` | Create reservation |
| PUT | `/api/bookings/{id}` | Update reservation |
| DELETE | `/api/bookings/{id}` | Cancel/delete reservation |

### Contracts — `/api/contracts`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/contracts` | Get all contracts |
| GET | `/api/contracts/{id}` | Get one contract |
| POST | `/api/contracts` | Create contract |
| PUT | `/api/contracts/{id}/sign` | Sign a contract |

### Invoices — `/api/invoices`
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/invoices` | Get all invoices |
| GET | `/api/invoices/{id}` | Get one invoice |
| POST | `/api/invoices` | Create invoice |
| PUT | `/api/invoices/{id}` | Update invoice |

---

## Data Flow: Booking a Camping Site

```
1. Camper browses  GET /api/sites/active
2. Camper selects a site and posts  POST /api/bookings
   { campingSite: {id}, checkInDate, checkOutDate, numberOfGuests }
3. System creates Reservation (status=PENDING)
4. Owner/Admin confirms  PUT /api/bookings/{id}  (status=CONFIRMED)
5. Contract generated  POST /api/contracts  (status=PENDING)
6. Camper signs  PUT /api/contracts/{id}/sign  (isSigned=true, status=ACTIVE)
7. Invoice issued  POST /api/invoices  (status=SENT)
8. Payment received  PUT /api/invoices/{id}  (status=PAID)
9. Check-in  PUT /api/bookings/{id}  (status=CHECKED_IN)
10. Check-out  PUT /api/bookings/{id}  (status=COMPLETED)
```
