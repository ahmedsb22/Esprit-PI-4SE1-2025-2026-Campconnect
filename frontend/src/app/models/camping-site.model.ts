export interface CampingSite {
  id?: number;
  name: string;
  description: string;
  location: string;
  address: string;
  pricePerNight: number;
  capacity: number;
  category?: string;
  imageUrl?: string;
  hasWifi?: boolean;
  hasParking?: boolean;
  hasRestrooms?: boolean;
  hasShowers?: boolean;
  hasElectricity?: boolean;
  hasPetFriendly?: boolean;
  isActive?: boolean;
  isVerified?: boolean;
  rating?: number;
  reviewCount?: number;
  owner?: any;
  amenities?: { name: string }[];
  images?: { url: string }[];
}

/** Flat view model used in the UI list/grid */
export interface CampingSiteView {
  id: number;
  name: string;
  location: string;
  description: string;
  price: number;
  rating: number;
  reviews: number;
  image: string;
  features: string[];
}
