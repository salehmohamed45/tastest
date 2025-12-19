# Men's Fashion E-Commerce Android App

A fully functional men's clothing e-commerce Android application built with Jetpack Compose and Firebase.

## Features

### User Authentication
- Email/password authentication via Firebase Auth
- Sign up with profile creation
- Login with email validation
- Persistent user sessions
- Sign out functionality

### User Roles
- **Customer Role**: Default role for all new users with access to shopping features
- **Admin Role**: Manually assigned role with additional privileges:
  - Add/edit/delete products
  - View all users
  - Manage all orders
  - Update order statuses

To assign admin role, update the `role` field in Firestore from "customer" to "admin".

### Product Browsing
- Grid layout displaying products (2 columns)
- Product cards showing: image, name, price, category
- Real-time product updates from Firestore
- Product availability status
- Category filtering

### Search & Filter
- Real-time search as user types
- Category filters (All, Shirts, Pants, Jackets, Shoes)
- Search by product name or description
- Empty state handling

### Shopping Cart
- Add products to cart with size selection
- Update item quantities
- Remove items from cart
- Real-time cart updates
- Persistent cart storage in Firestore
- Cart badge showing item count
- Total price calculation

### Checkout
- Order summary display
- Shipping address input (prefilled from profile)
- Order placement with Firebase
- Success confirmation dialog
- Order history tracking

### User Profile
- View and edit user information
- Update name, phone number, and address
- Order history display
- Order status tracking (Pending, Confirmed, Delivered)
- Profile data sync with Firestore

### Navigation
- Bottom navigation bar (Home, Search, Cart, Profile)
- Nested navigation for complex flows
- Proper back navigation handling
- Authentication-based routing

## Tech Stack

### Core Technologies
- **Kotlin**: Programming language
- **Jetpack Compose**: Modern UI toolkit
- **Material3**: Design system

### Architecture
- **MVVM**: Model-View-ViewModel pattern
- **Repository Pattern**: Data layer abstraction
- **StateFlow**: Reactive state management
- **Kotlin Coroutines**: Async operations

### Firebase Integration
- **Firebase Authentication**: User management
- **Cloud Firestore**: NoSQL database
- **Firebase BOM**: Version management

### Navigation
- **Jetpack Navigation Compose**: Type-safe navigation
- **Bottom Navigation**: Tab-based navigation
- **Nested Navigation**: Multi-level routing

### UI Components
- **Material3 Components**: Buttons, Cards, TextFields, etc.
- **Coil**: Image loading and caching
- **Custom Components**: ProductCard, CartItemCard, LoadingIndicator

## Project Structure

```
app/src/main/java/com/depi/drlist/
├── data/
│   ├── model/
│   │   ├── Product.kt              # Product data model
│   │   ├── CartItem.kt             # Shopping cart item model
│   │   ├── User.kt                 # User profile model
│   │   └── Order.kt                # Order model
│   └── repository/
│       ├── AuthRepository.kt       # Authentication operations
│       ├── ProductRepository.kt    # Product data operations
│       ├── CartRepository.kt       # Cart management
│       └── OrderRepository.kt      # Order operations
├── ui/
│   ├── components/
│   │   ├── ProductCard.kt          # Product display card
│   │   ├── CartItemCard.kt         # Cart item display
│   │   └── LoadingIndicator.kt     # Loading spinner
│   ├── screens/
│   │   ├── login/
│   │   │   ├── LoginScreen.kt      # Auth UI
│   │   │   └── LoginViewModel.kt   # Auth logic
│   │   ├── home/
│   │   │   ├── HomeScreen.kt       # Product grid
│   │   │   └── HomeViewModel.kt    # Product data
│   │   ├── search/
│   │   │   ├── SearchScreen.kt     # Search UI
│   │   │   └── SearchViewModel.kt  # Search logic
│   │   ├── cart/
│   │   │   ├── CartScreen.kt       # Cart UI
│   │   │   └── CartViewModel.kt    # Cart logic
│   │   ├── profile/
│   │   │   ├── ProfileScreen.kt    # Profile UI
│   │   │   └── ProfileViewModel.kt # Profile logic
│   │   └── checkout/
│   │       ├── CheckoutScreen.kt   # Checkout UI
│   │       └── CheckoutViewModel.kt# Checkout logic
│   ├── navigation/
│   │   ├── Route.kt                # Navigation routes
│   │   └── AppNavigation.kt        # Navigation setup
│   └── theme/
│       ├── Color.kt                # Color palette
│       ├── Theme.kt                # App theme
│       └── Type.kt                 # Typography
└── MainActivity.kt                  # App entry point
```

## Firestore Collections

### `users/{userId}`
User profile information

### `products/`
Product catalog with details, pricing, and availability

### `carts/{userId}/items/{productId}`
User shopping cart items

### `orders/{orderId}`
Order history and tracking

## Setup Instructions

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Android SDK with API level 28+

### Firebase Configuration
1. The app is already configured with Firebase project: `dr-list-5c34e`
2. `google-services.json` is included in the repository
3. Follow `FIREBASE_SETUP.md` for Firestore data setup

### Building the App
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run on emulator or physical device

```bash
./gradlew clean assembleDebug
```

### Adding Sample Products
Follow the instructions in `FIREBASE_SETUP.md` to populate Firestore with sample products.

## Design Decisions

### Color Scheme
- Professional men's fashion aesthetic
- Dark slate primary colors (0xFF2C3E50, 0xFF34495E)
- Silver/gray accents
- Gold accent for premium feel (0xFFC0A062)

### User Experience
- Clean, minimal interface
- Quick add-to-cart with size selection
- Persistent cart across sessions
- Real-time updates
- Smooth navigation transitions
- Loading states for all async operations
- Error handling with user feedback

### State Management
- `StateFlow` for reactive UI updates
- Sealed classes for UI states (Loading, Success, Error)
- ViewModels for business logic
- Repository pattern for data access

## Key Features Implementation

### Authentication Flow
- Check for existing user on app launch
- Redirect to Home if authenticated
- Redirect to Login if not authenticated
- Create user profile in Firestore on signup
- Sync Firebase Auth with Firestore

### Cart Management
- Real-time cart synchronization
- Quantity updates
- Size selection dialog
- Cart badge on navigation
- Persistent storage

### Order Processing
- Validate shipping address
- Create order in Firestore
- Clear cart after successful order
- Display confirmation
- Navigate to home

## Dependencies

Key dependencies in `app/build.gradle.kts`:
- Jetpack Compose BOM
- Firebase BOM (34.4.0)
- Firebase Auth
- Firebase Firestore
- Navigation Compose (2.7.7)
- Coil for image loading (2.6.0)
- Lifecycle ViewModel Compose (2.8.1)
- Material3 Icons Extended
- Coroutines Play Services (1.7.3)

## Testing

### Manual Testing Checklist
- [ ] User registration and login
- [ ] Product browsing
- [ ] Search functionality
- [ ] Category filtering
- [ ] Add to cart with size selection
- [ ] Cart quantity updates
- [ ] Cart item removal
- [ ] Checkout flow
- [ ] Order placement
- [ ] Profile editing
- [ ] Order history viewing
- [ ] Sign out functionality
- [ ] Navigation between screens
- [ ] Back button behavior

## Known Limitations

1. **Build Environment**: The project requires internet access to download Android Gradle Plugin and dependencies
2. **Image URLs**: Sample products use Unsplash placeholder images  
3. **Email Notifications**: Email service for order confirmations not yet implemented
4. **Admin Dashboard**: Admin panel for managing orders not yet implemented
5. **Offline Sync**: Room database infrastructure ready but sync logic pending implementation

## Recently Added Features (Latest Update)

### Password Reset ✅
- Complete password reset flow via email
- Firebase Auth integration
- Link from login screen

### Enhanced Product Catalog ✅
- Advanced filtering (size, color, brand, price range)
- Multiple sort options (price, newest)
- Filter and sort dialogs
- Updated product model with brand and colors

### Product Detail Screen ✅
- Dedicated product detail page
- Image display
- Size and color selection
- Quantity selector
- Stock status indicator
- Add to cart functionality

### Payment Integration ✅
- Multiple payment methods (Credit/Debit Card, COD, PayPal)
- Mock payment processing
- Payment method selection UI
- Processing states with loading indicators

### Room Database ✅
- Complete offline database infrastructure
- Entities for all data models
- DAOs with Flow-based queries
- Type converters for complex types
- Ready for offline-first implementation

## Future Enhancements

- Complete offline-first repository pattern implementation
- Data synchronization logic
- Email notifications for orders
- Admin dashboard for order management
- Product reviews and ratings
- Wishlist functionality
- Order tracking with push notifications
- Social media integration
- Size guide
- Product recommendations

## Security

### Firestore Security Rules
See `FIREBASE_SETUP.md` for complete security rules that:
- Allow public read access to products
- Restrict user data to authenticated owners
- Protect cart data per user
- Secure order creation and viewing

### Best Practices
- User data is scoped to authenticated users
- No sensitive data in client code
- Firebase configuration in `google-services.json`
- Proper error handling without exposing internals

## Contributing

This is a portfolio/demonstration project. For production use:
1. Replace placeholder images with actual product photos
2. Implement proper payment processing
3. Add comprehensive error logging
4. Implement analytics
5. Add unit and integration tests
6. Set up CI/CD pipeline

## License

This project is created for educational purposes.

## Contact

For questions or feedback, please create an issue in the repository.

---

**Note**: This app uses the existing Firebase configuration with project ID `dr-list-5c34e`. Make sure to set up Firestore with the sample data as described in `FIREBASE_SETUP.md`.
