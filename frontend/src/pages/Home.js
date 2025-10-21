import './Home.css';
import { Link } from 'react-router-dom';

function Home() {
    return (
        <div className="home-container">
            <div className="home-header">
                <h1>Welcome to Digital Banking</h1>
                <p>Manage your finances with ease and confidence</p>
            </div>

            <div className="features-grid">
                <div className="feature-card">
                    <h3>Dashboard</h3>
                    <p>Get a complete overview of your financial status at a glance</p>
                </div>
                <div className="feature-card">
                    <h3>Transactions</h3>
                    <p>Track and manage all your transactions in one place</p>
                </div>
                <div className="feature-card">
                    <h3>Budget</h3>
                    <p>Set and monitor your spending goals effectively</p>
                </div>
            </div>

            <center>
                <Link to="/login" className="cta-button">Get Started</Link>
            </center>
        </div>
    );
}

export default Home;
