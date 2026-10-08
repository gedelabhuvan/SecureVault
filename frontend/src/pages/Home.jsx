import React from "react";
import { useNavigate } from "react-router-dom";

function Home() {
    const navigate = useNavigate();

    const token = localStorage.getItem("token");

    // =========================================
    // LOGIN
    // =========================================

    function handleLogin() {
        navigate("/login");
    }

    // =========================================
    // LOGOUT
    // =========================================

    function handleLogout() {
        // Remove authentication information
        localStorage.removeItem("token");
        localStorage.removeItem("username");
        localStorage.removeItem("email");

        // After logout, return to Home page
        navigate("/", {
            replace: true
        });

        // Refresh page so login status updates immediately
        window.location.reload();
    }

    // =========================================
    // PERSONAL VAULT
    // =========================================

    function handlePersonalVault() {
        const currentToken = localStorage.getItem("token");

        // If user is not logged in,
        // send them to Login page
        if (!currentToken) {
            navigate("/login", {
                replace: true
            });

            return;
        }

        // If logged in, open Personal Vault
        navigate("/vault");
    }

    // =========================================
    // TEAM VAULT
    // =========================================

    function handleTeamVault() {
        const currentToken = localStorage.getItem("token");

        // If user is not logged in,
        // send them to Login page
        if (!currentToken) {
            navigate("/login", {
                replace: true
            });

            return;
        }

        // If logged in, open Team Vault
        navigate("/team-vault");
    }

    // =========================================
    // LOGIN ACTIVITY
    // =========================================

    function handleLoginActivity() {
        const currentToken = localStorage.getItem("token");

        // Login Activity is protected
        if (!currentToken) {
            navigate("/login", {
                replace: true
            });

            return;
        }

        // If logged in, open Login Activity
        navigate("/login-activity");
    }

    return (
        <div className="min-h-screen bg-slate-950 text-white">

            {/* =====================================
                HEADER
               ===================================== */}

            <header className="border-b border-slate-800">

                <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-5">

                    {/* Logo / Brand */}

                    <div className="flex items-center gap-4">

                        <div className="flex h-14 w-14 items-center justify-center rounded-2xl bg-blue-500 text-3xl shadow-lg">
                            🔐
                        </div>

                        <div>

                            <h1 className="text-2xl font-bold text-cyan-400">
                                SecureVault
                            </h1>

                            <p className="text-sm text-slate-400">
                                Your secure password management system
                            </p>

                        </div>

                    </div>


                    {/* =================================
                        LOGIN / LOGOUT BUTTONS
                       ================================= */}

                    <div className="flex items-center gap-3">

                        {!token && (
                            <button
                                onClick={handleLogin}
                                className="rounded-lg border border-cyan-500 px-5 py-2.5 font-semibold text-cyan-400 transition hover:bg-cyan-500 hover:text-white"
                            >
                                🔑 Login
                            </button>
                        )}

                        {token && (
                            <button
                                onClick={handleLogout}
                                className="rounded-lg border border-red-500 px-5 py-2.5 font-semibold text-red-400 transition hover:bg-red-500 hover:text-white"
                            >
                                🚪 Logout
                            </button>
                        )}

                    </div>

                </div>

            </header>


            {/* =====================================
                MAIN CONTENT
               ===================================== */}

            <main className="mx-auto max-w-7xl px-6 py-16">


                {/* =================================
                    HERO SECTION
                   ================================= */}

                <section className="text-center">

                    <div className="mb-6 text-7xl">
                        🔐
                    </div>


                    <h2 className="text-4xl font-bold md:text-5xl">

                        Welcome to{" "}

                        <span className="text-cyan-400">
                            SecureVault
                        </span>

                    </h2>


                    <p className="mx-auto mt-6 max-w-3xl text-lg leading-8 text-slate-400">

                        Securely store, manage, generate, and share
                        your credentials with powerful password
                        security and controlled access.

                    </p>


                    {/* LOGIN STATUS */}

                    <div className="mt-6">

                        {token ? (

                            <span className="inline-flex items-center gap-2 rounded-full border border-green-500/30 bg-green-500/10 px-4 py-2 text-sm text-green-400">

                                <span className="h-2 w-2 rounded-full bg-green-400"></span>

                                You are logged in

                            </span>

                        ) : (

                            <span className="inline-flex items-center gap-2 rounded-full border border-yellow-500/30 bg-yellow-500/10 px-4 py-2 text-sm text-yellow-400">

                                🔒 Please login to access your vaults

                            </span>

                        )}

                    </div>

                </section>


                {/* =================================
                    VAULT & SECURITY OPTIONS
                   ================================= */}

               <section className="mt-16 grid grid-cols-1 lg:grid-cols-3 gap-8">


                    {/* =================================
                        PERSONAL VAULT
                       ================================= */}

                    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl transition duration-300 hover:border-blue-500 hover:shadow-blue-500/10">

                        <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-blue-500/20 text-5xl">
                            🔑
                        </div>


                        <h3 className="mt-7 text-2xl font-bold">
                            Personal Vault
                        </h3>


                        <p className="mt-4 leading-7 text-slate-400">

                            Securely store and manage your personal
                            credentials. Passwords are protected using
                            encryption and authentication.

                        </p>


                        <button
                            onClick={handlePersonalVault}
                            className="mt-8 w-full rounded-xl bg-blue-600 px-6 py-3.5 font-semibold transition hover:bg-blue-500"
                        >
                            🔑 Open Personal Vault
                        </button>

                    </div>


                    {/* =================================
                        TEAM VAULT
                       ================================= */}

                    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl transition duration-300 hover:border-purple-500 hover:shadow-purple-500/10">

                        <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-purple-500/20 text-5xl">
                            👥
                        </div>


                        <h3 className="mt-7 text-2xl font-bold">
                            Team Vault
                        </h3>


                        <p className="mt-4 leading-7 text-slate-400">

                            Collaborate securely with your team.
                            Share credentials using View Only,
                            Edit Access, and Full Management permissions.

                        </p>


                        <button
                            onClick={handleTeamVault}
                            className="mt-8 w-full rounded-xl bg-purple-600 px-6 py-3.5 font-semibold transition hover:bg-purple-500"
                        >
                            👥 Open Team Vault
                        </button>

                    </div>


                    {/* =================================
                        LOGIN ACTIVITY
                       ================================= */}

                    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl transition duration-300 hover:border-cyan-500 hover:shadow-cyan-500/10">

                        <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-cyan-500/20 text-5xl">
                            🛡️
                        </div>


                        <h3 className="mt-7 text-2xl font-bold">
                            Login Activity
                        </h3>


                        <p className="mt-4 leading-7 text-slate-400">

                            Monitor your login attempts, authentication
                            status, IP address, device, and browser activity.

                        </p>


                        <button
                            onClick={handleLoginActivity}
                            className="mt-8 w-full rounded-xl bg-cyan-600 px-6 py-3.5 font-semibold transition hover:bg-cyan-500"
                        >
                            🛡️ View Login Activity
                        </button>


                    </div>
                    {/* Security Alerts */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 shadow-xl transition duration-300 hover:border-red-500/40">

    <div className="flex h-20 w-20 items-center justify-center rounded-2xl bg-red-500/20 text-5xl">
        🚨
    </div>

    <h3 className="mt-7 text-2xl font-bold">
        Security Alerts
    </h3>

    <p className="mt-4 leading-7 text-slate-400">
        Monitor and manage security alerts, suspicious activities,
        and important security events.
    </p>

    <button
        onClick={() => navigate("/security-alerts")}
        className="mt-6 w-full rounded-xl bg-red-600 px-6 py-3.5 font-semibold transition hover:bg-red-500"
    >
        🚨 View Security Alerts
    </button>

</div>

                </section>


                {/* =================================
                    SECURITY FEATURES
                   ================================= */}

                <section className="mt-16">

                    <h3 className="mb-8 text-center text-2xl font-bold">
                        SecureVault Features
                    </h3>


                    <div className="grid gap-6 md:grid-cols-3">


                        {/* Encryption */}

                        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-7 text-center">

                            <div className="text-4xl">
                                🔒
                            </div>

                            <h4 className="mt-4 text-lg font-semibold">
                                Encrypted Storage
                            </h4>

                            <p className="mt-3 text-sm leading-6 text-slate-400">

                                Sensitive credential passwords are
                                protected using encryption.

                            </p>

                        </div>


                        {/* Password Generator */}

                        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-7 text-center">

                            <div className="text-4xl">
                                🔑
                            </div>

                            <h4 className="mt-4 text-lg font-semibold">
                                Password Generator
                            </h4>

                            <p className="mt-3 text-sm leading-6 text-slate-400">

                                Generate strong passwords with
                                customizable security options.

                            </p>

                        </div>


                        {/* Access Control */}

                        <div className="rounded-xl border border-slate-800 bg-slate-900/70 p-7 text-center">

                            <div className="text-4xl">
                                🛡️
                            </div>

                            <h4 className="mt-4 text-lg font-semibold">
                                Access Control
                            </h4>

                            <p className="mt-3 text-sm leading-6 text-slate-400">

                                Control shared credential access using
                                permission levels and expiration.

                            </p>

                        </div>

                    </div>

                </section>


                {/* =================================
                    LOGIN MESSAGE
                   ================================= */}

                {!token && (

                    <section className="mt-16 rounded-2xl border border-cyan-500/30 bg-cyan-500/5 p-8 text-center">

                        <h3 className="text-2xl font-bold">
                            🔐 Access SecureVault
                        </h3>


                        <p className="mt-3 text-slate-400">

                            Click Personal Vault, Team Vault, or
                            Login Activity to login and access
                            your secure information.

                        </p>


                        <button
                            onClick={handleLogin}
                            className="mt-6 rounded-xl bg-cyan-600 px-8 py-3 font-semibold transition hover:bg-cyan-500"
                        >
                            🔑 Login to SecureVault
                        </button>

                    </section>

                )}


            </main>


            {/* =====================================
                FOOTER
               ===================================== */}

            <footer className="border-t border-slate-800">

                <div className="mx-auto max-w-7xl px-6 py-8 text-center">

                    <p className="text-sm text-slate-500">
                        SecureVault — Secure Password Management System
                    </p>

                    <p className="mt-2 text-xs text-slate-600">
                        Your credentials. Your security. Your control.
                    </p>

                </div>
                 {/* Security Overview */}
<section className="mt-16">
    <h2 className="mb-8 text-center text-2xl font-bold">
        Security Overview
    </h2>

    <div className="grid gap-6 md:grid-cols-3">

        {/* Password Health */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
            <div className="text-4xl">🔐</div>

            <h3 className="mt-5 text-xl font-bold">
                Password Health
            </h3>

            <p className="mt-3 text-sm leading-6 text-slate-400">
                Review your saved credentials and maintain strong,
                secure passwords.
            </p>

            <button
                onClick={() => navigate("/vault")}
                className="mt-5 w-full rounded-xl bg-cyan-600 px-5 py-3 font-semibold transition hover:bg-cyan-500"
            >
                Check Password Health
            </button>
        </div>

        {/* Security Analytics */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
            <div className="text-4xl">📊</div>

            <h3 className="mt-5 text-xl font-bold">
                Security Analytics
            </h3>

            <p className="mt-3 text-sm leading-6 text-slate-400">
                View login statistics, security activity, and
                security insights.
            </p>

            <button
                onClick={() => navigate("/security-analytics")}
                className="mt-5 w-full rounded-xl bg-purple-600 px-5 py-3 font-semibold transition hover:bg-purple-500"
            >
                View Analytics
            </button>
        </div>

        {/* Security Alerts */}
        <div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
            <div className="text-4xl">🚨</div>

            <h3 className="mt-5 text-xl font-bold">
                Security Alerts
            </h3>

            <p className="mt-3 text-sm leading-6 text-slate-400">
                Monitor suspicious activities and important
                security alerts.
            </p>

            <button
                onClick={() => navigate("/security-alerts")}
                className="mt-5 w-full rounded-xl bg-red-600 px-5 py-3 font-semibold transition hover:bg-red-500"
            >
                View Alerts
            </button>
        </div>

        {/* Device Management */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
    <div className="text-4xl">📱</div>

    <h3 className="mt-5 text-xl font-bold">
        Device Management
    </h3>

    <p className="mt-3 text-sm leading-6 text-slate-400">
        View, trust, untrust, and remove devices connected
        to your SecureVault account.
    </p>

    <button
        onClick={() => navigate("/devices")}
        className="mt-5 w-full rounded-xl bg-blue-600 px-5 py-3 font-semibold transition hover:bg-blue-500"
    >
        Manage Devices
    </button>
</div>

{/* Active Sessions */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
    <div className="text-4xl">🖥️</div>

    <h3 className="mt-5 text-xl font-bold">
        Active Sessions
    </h3>

    <p className="mt-3 text-sm leading-6 text-slate-400">
        View and manage your active login sessions and revoke unwanted sessions.
    </p>

    <button
        onClick={() => navigate("/sessions")}
        className="mt-5 w-full rounded-xl bg-cyan-600 px-5 py-3 font-semibold transition hover:bg-cyan-500"
    >
        Manage Sessions
    </button>
</div>

{/* Notifications */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
    <div className="text-4xl">🔔</div>

    <h3 className="mt-5 text-xl font-bold">
        Notifications
    </h3>

    <p className="mt-3 text-sm leading-6 text-slate-400">
        View security, login, sharing, and account notifications.
    </p>

    <button
        onClick={() => navigate("/notifications")}
        className="mt-5 w-full rounded-xl bg-cyan-600 px-5 py-3 font-semibold transition hover:bg-cyan-500"
    >
        View Notifications
    </button>
</div>

{/* Admin User Management */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-7 shadow-xl">
    <div className="text-4xl">👤</div>

    <h3 className="mt-5 text-xl font-bold">
        User Management
    </h3>

    <p className="mt-3 text-sm leading-6 text-slate-400">
        View registered users and manage account roles.
    </p>

    <button
        onClick={() => navigate("/admin/users")}
        className="mt-5 w-full rounded-xl bg-cyan-600 px-5 py-3 font-semibold transition hover:bg-cyan-500"
    >
        Manage Users
    </button>
</div>

{/* Reports */}
<div className="rounded-2xl border border-slate-800 bg-slate-900 p-6">
    <div className="text-4xl">📊</div>

    <h3 className="mt-5 text-xl font-bold">
        Reports
    </h3>

    <p className="mt-3 text-sm leading-6 text-slate-400">
        Export your credential data and download CSV reports.
    </p>

    <button
        onClick={() => navigate("/reports")}
        className="mt-5 w-full rounded-xl bg-cyan-600 px-5 py-3 font-semibold hover:bg-cyan-500"
    >
        View Reports
    </button>
</div>

    </div>
</section>


            </footer>

           

        </div>
    );
}

export default Home;