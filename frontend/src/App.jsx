import React from "react";
import SecurityAnalytics from "./pages/SecurityAnalytics";
import OAuth2Success from "./pages/OAuth2Success";
import {
    BrowserRouter,
    Routes,
    Route,
    Navigate,
} from "react-router-dom";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Home from "./pages/Home";
import Vault from "./pages/Vault";
import TeamVault from "./pages/TeamVault";
import LoginActivity from "./pages/LoginActivity";

function ProtectedRoute({ children }) {
    const token = localStorage.getItem("token");

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return children;
}

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* Home */}
                <Route
                    path="/"
                    element={<Home />}
                />

                {/* Authentication */}
                <Route
                    path="/login"
                    element={<Login />}
                />

                <Route
                    path="/oauth2/success"
                    element={<OAuth2Success />}
                />

                <Route
                    path="/register"
                    element={<Register />}
                />

                {/* Personal Vault */}
                <Route
                    path="/vault"
                    element={
                        <ProtectedRoute>
                            <Vault />
                        </ProtectedRoute>
                    }
                />

                {/* Team Vault */}
                <Route
                    path="/team-vault"
                    element={
                        <ProtectedRoute>
                            <TeamVault />
                        </ProtectedRoute>
                    }
                />

                {/* Login Activity / Security Monitoring */}
                <Route
                    path="/login-activity"
                    element={
                        <ProtectedRoute>
                            <LoginActivity />
                        </ProtectedRoute>
                    }
                />

                {/* Security Analytics */}
<Route
    path="/security-analytics"
    element={
        <ProtectedRoute>
            <SecurityAnalytics />
        </ProtectedRoute>
    }
/>

                {/* Unknown routes → Home */}
                <Route
                    path="*"
                    element={<Navigate to="/" replace />}
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;