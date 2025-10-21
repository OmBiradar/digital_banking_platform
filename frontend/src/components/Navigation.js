// components/Navigation.js
import { Link } from 'react-router-dom';
import './Navigation.css';

function Navigation() {
    return (
        <nav className="navigation">
            <div className="nav-container">
                <Link to="/" className="nav-brand">Banking App</Link>
                <ul className="nav-links">
                    <li><Link to="/">Home</Link></li>
                    <li><Link to="/login">Login</Link></li>
                    {/* <li><Link to="/dashboard">Dashboard</Link></li>
                    <li><Link to="/transactions">Transactions</Link></li>
                    <li><Link to="/budget">Budget</Link></li> */}
                </ul>
            </div>
        </nav>
    );
}

export default Navigation;
