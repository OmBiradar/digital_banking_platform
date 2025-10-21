import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Navigation from './components/Navigation.js';
import Home from './pages/Home.js';
import Login from './pages/Login.js';
// import Dashboard from "./pages/Dashboard";
// import Transactions from "./pages/Transactions";
// import Budget from './pages/Budget';

function App() {
    return (
        <BrowserRouter>
            <Navigation />
            <Routes>
                <Route path="/" element={<Home />} />
                <Route path="/login" element={<Login />} />
                {/* <Route path="/dashboard" element={<Dashboard />} />
                <Route path="/transactions" element={<Transactions />} />
                <Route path="/budget" element={<Budget />} /> */}
                <Route path="*" element={<Navigate to="/" />} />
            </Routes>
        </BrowserRouter>
    );
}

export default App;
