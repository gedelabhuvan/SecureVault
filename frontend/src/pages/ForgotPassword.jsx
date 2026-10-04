import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function ForgotPassword() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const [resetToken, setResetToken] = useState("");

    const handleForgotPassword = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");
        setResetToken("");
        setLoading(true);

        try {
            const response = await fetch(
                "http://localhost:8080/api/auth/forgot-password",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                    },
                    body: JSON.stringify({
                        email: email.trim(),
                    }),
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Unable to process password reset request."
                );
            }

            setMessage(
                data.message ||
                "Password reset request processed successfully."
            );

            /*
             * Development/testing only.
             *
             * The backend currently returns the reset token
             * so that the complete reset flow can be tested
             * without an email service.
             */
            if (data.resetToken) {
                setResetToken(data.resetToken);
            }

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const handleContinue = () => {
        if (!resetToken) {
            return;
        }

        navigate(
            `/reset-password?token=${encodeURIComponent(resetToken)}`
        );
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center px-4 relative overflow-hidden">

            {/* Background glow */}
            <div className="absolute -top-40 -left-40 w-96 h-96 bg-cyan-500/20 rounded-full blur-3xl"></div>

            <div className="absolute -bottom-40 -right-40 w-96 h-96 bg-violet-600/20 rounded-full blur-3xl"></div>

            <div className="relative z-10 w-full max-w-md">

                <div className="bg-slate-900/80 backdrop-blur-xl border border-slate-700/60 rounded-3xl shadow-2xl p-8 sm:p-10">

                    {/* Logo */}
                    <div className="flex justify-center mb-6">
                        <div className="w-20 h-20 rounded-2xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
                            <span className="text-4xl">
                                🔑
                            </span>
                        </div>
                    </div>

                    {/* Heading */}
                    <div className="text-center mb-8">

                        <h1 className="text-3xl font-bold text-cyan-400">
                            Forgot Password
                        </h1>

                        <p className="text-slate-400 mt-3 text-sm">
                            Enter your registered email address
                            to reset your password.
                        </p>

                    </div>

                    {/* Form */}
                    <form
                        onSubmit={handleForgotPassword}
                        className="space-y-5"
                    >

                        {/* Email */}
                        <div>

                            <label
                                htmlFor="forgot-email"
                                className="block text-sm font-medium text-slate-300 mb-2"
                            >
                                Email
                            </label>

                            <input
                                id="forgot-email"
                                type="email"
                                value={email}
                                onChange={(event) =>
                                    setEmail(event.target.value)
                                }
                                placeholder="Enter your registered email"
                                required
                                className="w-full h-12 px-4 rounded-xl bg-slate-950/70 border border-slate-700 text-white placeholder-slate-500 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                            />

                        </div>

                        {/* Error */}
                        {error && (
                            <div className="rounded-xl border border-red-500/30 bg-red-500/10 px-4 py-3 text-sm text-red-400">
                                {error}
                            </div>
                        )}

                        {/* Success */}
                        {message && (
                            <div className="rounded-xl border border-green-500/30 bg-green-500/10 px-4 py-3 text-sm text-green-400">
                                {message}
                            </div>
                        )}

                        {/* Development Token */}
                        {resetToken && (
                            <div className="rounded-xl border border-yellow-500/30 bg-yellow-500/10 p-4">

                                <p className="text-xs text-yellow-400 font-semibold mb-2">
                                    Development Reset Token
                                </p>

                                <p className="text-xs text-slate-300 break-all">
                                    {resetToken}
                                </p>

                                <button
                                    type="button"
                                    onClick={handleContinue}
                                    className="w-full mt-4 h-11 rounded-xl bg-yellow-500 text-slate-950 font-semibold hover:bg-yellow-400 transition"
                                >
                                    Continue to Reset Password
                                </button>

                            </div>
                        )}

                        {/* Submit */}
                        {!resetToken && (
                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full h-12 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 text-white font-semibold shadow-lg shadow-cyan-500/20 transition duration-200 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                {loading
                                    ? "Processing..."
                                    : "Send Reset Request"}
                            </button>
                        )}

                    </form>

                    {/* Back */}
                    <div className="text-center mt-7 text-sm">

                        <Link
                            to="/login"
                            className="text-cyan-400 hover:text-cyan-300 transition font-semibold"
                        >
                            ← Back to Login
                        </Link>

                    </div>

                </div>

                <p className="text-center text-xs text-slate-600 mt-6">
                    SecureVault • Password Management System
                </p>

            </div>

        </div>
    );
}

export default ForgotPassword;