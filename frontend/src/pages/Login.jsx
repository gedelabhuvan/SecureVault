import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleGoogleLogin = () => {
    window.location.href =
        "http://localhost:8080/oauth2/authorization/google";
};

    const handleLogin = async (event) => {
        event.preventDefault();

        setError("");
        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/login",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        email: email,
                        password: password,
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Login failed"
                );
            }

            if (!data.token) {
                throw new Error(
                    "Login successful, but authentication token was not received."
                );
            }

            // Save JWT token
            localStorage.setItem("token", data.token);

            // Save email
            localStorage.setItem("email", email);

            // Go to Dashboard / Home page
            navigate("/", { replace: true });

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center px-4 relative overflow-hidden">

            {/* Background glow */}
            <div className="absolute -top-40 -left-40 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl"></div>

            <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-violet-600/20 rounded-full blur-3xl"></div>

            <div className="absolute top-1/2 left-1/2 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2"></div>

            {/* Login Card */}
            <div className="relative z-10 w-full max-w-md">

                <div className="bg-slate-900/80 backdrop-blur-xl border border-slate-700/60 rounded-3xl shadow-2xl p-8 sm:p-10">

                    {/* Back to Dashboard */}
                    <div className="mb-6">

                        <button
                            type="button"
                            onClick={() => navigate("/")}
                            className="text-sm text-cyan-400 hover:text-cyan-300 transition font-medium"
                        >
                            ← Back to Dashboard
                        </button>

                    </div>

                    {/* Logo */}
                    <div className="flex justify-center mb-6">

                        <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">

                            <span className="text-4xl">
                                🔐
                            </span>

                        </div>

                    </div>

                    {/* Heading */}
                    <div className="text-center mb-8">

                        <h1 className="text-4xl font-bold tracking-tight bg-gradient-to-r from-cyan-400 to-blue-500 bg-clip-text text-transparent">
                            SecureVault
                        </h1>

                        <p className="text-slate-400 mt-3 text-sm">
                            Secure your digital credentials
                        </p>

                    </div>

                    {/* Login Form */}
                    <form
                        onSubmit={handleLogin}
                        className="space-y-5"
                    >

                        {/* Email */}
                        <div>

                            <label
                                htmlFor="email"
                                className="block text-sm font-medium text-slate-300 mb-2"
                            >
                                Email
                            </label>

                            <input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                placeholder="Enter your email"
                                required
                                className="w-full h-12 px-4 rounded-xl bg-slate-950/70 border border-slate-700 text-white placeholder-slate-500 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                            />

                        </div>

                        {/* Password */}
                        <div>

                            <label
                                htmlFor="password"
                                className="block text-sm font-medium text-slate-300 mb-2"
                            >
                                Password
                            </label>

                            <input
                                id="password"
                                type="password"
                                value={password}
                                onChange={(event) =>
                                    setPassword(event.target.value)
                                }
                                placeholder="Enter your password"
                                required
                                className="w-full h-12 px-4 rounded-xl bg-slate-950/70 border border-slate-700 text-white placeholder-slate-500 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                            />

                        </div>

                        {/* Forgot Password */}
                        <div className="flex justify-end -mt-2">

                            <Link
                                to="/forgot-password"
                                className="text-sm text-cyan-400 hover:text-cyan-300 transition"
                            >
                                Forgot Password?
                            </Link>

                        </div>

                        {/* Error */}
                        {error && (
                            <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
                                {error}
                            </div>
                        )}

                        {/* Login Button */}
                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full h-12 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 text-white font-semibold shadow-lg shadow-cyan-500/20 transition duration-200 hover:-translate-y-0.5 hover:shadow-cyan-500/30 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:translate-y-0"
                        >
                            {loading
                                ? "Logging in..."
                                : "Login"}
                        </button>

                        {/* Google Login Button */}
                        <button
    type="button"
    onClick={handleGoogleLogin}
>
    Continue with Google
</button>

                    </form>

                    {/* Register Link */}
                    <div className="text-center mt-7 text-sm text-slate-400">

                        Don't have an account?{" "}

                        <Link
                            to="/register"
                            className="text-cyan-400 font-semibold hover:text-cyan-300 transition"
                        >
                            Create Account
                        </Link>

                    </div>

                </div>

                {/* Footer */}
                <p className="text-center text-xs text-slate-600 mt-6">
                    SecureVault • Password Management System
                </p>

            </div>

        </div>
    );
}

export default Login;