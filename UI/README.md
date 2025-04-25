# Fingerprint Management System

## Overview
A modern, React-based web application for managing employee access and fingerprint samples.

## Features
- Employee access management
- Fingerprint sample tracking
- Enable/disable fingerprint samples
- Set maximum fingerprint samples per employee

## Technologies Used
- React
- TypeScript
- Tailwind CSS
- Lucide React Icons

## Prerequisites
- Node.js (v14 or later)
- npm or yarn

## Installation

1. Clone the repository
```bash
git clone https://github.com/your-username/fingerprint-management-system.git
cd fingerprint-management-system
```

2. Install dependencies
```bash
npm install
# or
yarn installnpm 
```

3. Set up environment variables
Create a `.env` file in the project root and add:
```
REACT_APP_API_BASE_URL=http://localhost:8080/api
```

## Running the Application

### Development Mode
```bash
npm start
# or
yarn start
```
Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### Building for Production
```bash
npm run build
# or
yarn build
```

## Project Structure
```
src/
│
├── components/
│   ├── EmployeeSelection.tsx
│   ├── AreaAccessManagement.tsx
│   └── FingerprintManagement.tsx
│
├── config/
│   └── api.ts
│
├── hooks/
│   └── useToast.ts
│
├── App.tsx
└── index.tsx
```

## API Dependencies
This application requires a backend API with the following endpoints:
- `/employee` - Retrieve employee list
- `/area` - Retrieve areas
- `/access/by-employee/{employeeId}` - Get access permissions
- `/fingerprint-sample/employee/{employeeId}` - Get fingerprint samples

## Customization
- Modify Tailwind configuration in `tailwind.config.js`
- Adjust API endpoints in `src/config/api.ts`

## Testing
```bash
npm test
# or 
yarn test
```

## Deployment
- Build the application with `npm run build`
- Deploy the contents of the `build/` directory to your preferred hosting platform

## Contributing
1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License
Distributed under the MIT License. See `LICENSE` for more information.

## Contact
Your Name - your.email@example.com

Project Link: [https://github.com/your-username/fingerprint-management-system](https://github.com/your-username/fingerprint-management-system)