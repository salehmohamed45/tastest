# Firebase Firestore Setup for Men's Fashion E-Commerce App

## Firestore Collections Structure

### 1. `users` Collection
Path: `/users/{userId}`

Fields:
- `uid`: String (User ID from Firebase Auth)
- `email`: String
- `name`: String
- `phoneNumber`: String (optional)
- `address`: String (optional)

### 2. `products` Collection
Path: `/products/{productId}`

Fields:
- `name`: String
- `description`: String
- `price`: Number (Double)
- `imageUrl`: String (URL to product image)
- `category`: String (e.g., "Shirts", "Pants", "Jackets", "Shoes")
- `sizes`: Array of Strings (e.g., ["S", "M", "L", "XL"])
- `inStock`: Boolean

### 3. `carts` Collection
Path: `/carts/{userId}/items/{productId}`

Fields:
- `productId`: String
- `product`: Map (Product object)
- `quantity`: Number (Integer)
- `selectedSize`: String
- `totalPrice`: Number (Double)

### 4. `orders` Collection
Path: `/orders/{orderId}`

Fields:
- `orderId`: String
- `userId`: String
- `items`: Array of Maps (CartItem objects)
- `totalAmount`: Number (Double)
- `orderDate`: Timestamp
- `status`: String (e.g., "Pending", "Confirmed", "Delivered")
- `shippingAddress`: String

## Sample Products Data

### To add these products to Firestore:

1. Go to Firebase Console
2. Navigate to Firestore Database
3. Create a collection named `products`
4. Add the following documents:

#### Product 1: Classic Oxford Shirt
```json
{
  "name": "Classic Oxford Shirt",
  "description": "Premium cotton oxford shirt with button-down collar. Perfect for both formal and casual occasions.",
  "price": 79.99,
  "imageUrl": "https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=400",
  "category": "Shirts",
  "sizes": ["S", "M", "L", "XL", "XXL"],
  "inStock": true
}
```

#### Product 2: Slim Fit Chinos
```json
{
  "name": "Slim Fit Chinos",
  "description": "Comfortable stretch chinos with a modern slim fit. Available in multiple colors.",
  "price": 89.99,
  "imageUrl": "https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=400",
  "category": "Pants",
  "sizes": ["28", "30", "32", "34", "36"],
  "inStock": true
}
```

#### Product 3: Leather Bomber Jacket
```json
{
  "name": "Leather Bomber Jacket",
  "description": "Genuine leather bomber jacket with ribbed cuffs and hem. Timeless classic style.",
  "price": 299.99,
  "imageUrl": "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400",
  "category": "Jackets",
  "sizes": ["S", "M", "L", "XL"],
  "inStock": true
}
```

#### Product 4: Premium Sneakers
```json
{
  "name": "Premium Sneakers",
  "description": "Comfortable everyday sneakers with premium leather upper and cushioned sole.",
  "price": 129.99,
  "imageUrl": "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400",
  "category": "Shoes",
  "sizes": ["8", "9", "10", "11", "12"],
  "inStock": true
}
```

#### Product 5: Denim Shirt
```json
{
  "name": "Denim Shirt",
  "description": "Classic denim shirt in medium wash. Versatile piece for layering or wearing solo.",
  "price": 69.99,
  "imageUrl": "https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=400",
  "category": "Shirts",
  "sizes": ["S", "M", "L", "XL"],
  "inStock": true
}
```

#### Product 6: Cargo Pants
```json
{
  "name": "Cargo Pants",
  "description": "Modern cargo pants with multiple pockets. Comfortable and practical.",
  "price": 94.99,
  "imageUrl": "https://images.unsplash.com/photo-1506629082955-511b1aa562c8?w=400",
  "category": "Pants",
  "sizes": ["28", "30", "32", "34", "36"],
  "inStock": true
}
```

#### Product 7: Wool Blazer
```json
{
  "name": "Wool Blazer",
  "description": "Tailored wool blazer for a sharp, professional look. Dry clean only.",
  "price": 249.99,
  "imageUrl": "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=400",
  "category": "Jackets",
  "sizes": ["S", "M", "L", "XL"],
  "inStock": true
}
```

#### Product 8: Dress Shoes
```json
{
  "name": "Leather Dress Shoes",
  "description": "Classic leather dress shoes. Perfect for formal occasions and business wear.",
  "price": 159.99,
  "imageUrl": "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=400",
  "category": "Shoes",
  "sizes": ["8", "9", "10", "11", "12"],
  "inStock": true
}
```

#### Product 9: Polo Shirt
```json
{
  "name": "Classic Polo Shirt",
  "description": "Cotton pique polo shirt with embroidered logo. Smart casual essential.",
  "price": 59.99,
  "imageUrl": "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=400",
  "category": "Shirts",
  "sizes": ["S", "M", "L", "XL", "XXL"],
  "inStock": true
}
```

#### Product 10: Athletic Joggers
```json
{
  "name": "Athletic Joggers",
  "description": "Comfortable joggers with tapered fit and elastic waistband. Perfect for workouts or casual wear.",
  "price": 69.99,
  "imageUrl": "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400",
  "category": "Pants",
  "sizes": ["S", "M", "L", "XL"],
  "inStock": true
}
```

## Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow all users to read products
    match /products/{productId} {
      allow read: if true;
      allow write: if false; // Products should be managed through admin panel
    }
    
    // Users can only access their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Users can only access their own cart
    match /carts/{userId}/items/{itemId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Users can only read their own orders
    match /orders/{orderId} {
      allow read: if request.auth != null && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
      allow update: if false; // Orders cannot be updated by users
    }
  }
}
```

## Setup Instructions

1. **Enable Firestore**: In Firebase Console, enable Firestore Database in production mode
2. **Add Sample Products**: Use the Firebase Console to add the sample products above
3. **Configure Security Rules**: Update the Firestore security rules as shown above
4. **Enable Authentication**: Ensure Email/Password authentication is enabled in Firebase Auth
5. **Test**: Create a user account and test the app functionality

## Optional: Batch Import Script

You can create a Node.js script to batch import products:

```javascript
const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const products = [ /* paste the products array from above */ ];

async function importProducts() {
  const batch = db.batch();
  
  products.forEach((product) => {
    const docRef = db.collection('products').doc();
    batch.set(docRef, product);
  });
  
  await batch.commit();
  console.log('Products imported successfully!');
}

importProducts();
```

## Notes

- The `imageUrl` fields use Unsplash placeholder images. Replace with actual product images for production.
- Product IDs are auto-generated by Firestore unless specified otherwise.
- Ensure the Firebase project ID matches `dr-list-5c34e` as configured in `google-services.json`.
