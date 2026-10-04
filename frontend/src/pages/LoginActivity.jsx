import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

function LoginActivity() {
    const navigate = useNavigate();

    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchLoginHistory = async () => {
            const token = localStorage.getItem("token");

            if (!token) {
                navigate("/login", { replace: true });
                return;
            }

            try {
                const response = await fetch(
                    "http://localhost:8080/api/security/login-history",
                    {
                        method: "GET",
                        headers: {
                            Authorization: `Bearer ${token}`,
                        },
                    }
                );

                if (response.status === 401 || response.status === 403) {
                    localStorage.removeItem("token");
                    navigate("/login", { replace: true });
                    return;
                }

                if (!response.ok) {
                    throw new Error("Failed to load login activity.");
                }

                const data = await response.json();
                setEvents(data);
            } catch (err) {
                setError(err.message || "Unable to load login activity.");
            } finally {
                setLoading(false);
            }
        };

        fetchLoginHistory();
    }, [navigate]);

    const formatDateTime = (dateTime) => {
        if (!dateTime) {
            return "N/A";
        }

        const date = new Date(dateTime);

        if (Number.isNaN(date.getTime())) {
            return dateTime;
        }

        return date.toLocaleString("en-IN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
            second: "2-digit",
        });
    };

    const getBrowserName = (userAgent) => {
        if (!userAgent) {
            return "Unknown";
        }

        if (userAgent.includes("Edg/")) {
            return "Microsoft Edge";
        }

        if (userAgent.includes("Chrome/")) {
            return "Google Chrome";
        }

        if (userAgent.includes("Firefox/")) {
            return "Mozilla Firefox";
        }

        if (userAgent.includes("Safari/") && !userAgent.includes("Chrome/")) {
            return "Safari";
        }

        if (userAgent.includes("OPR/")) {
            return "Opera";
        }

        return "Unknown Browser";
    };

    const getDeviceName = (userAgent) => {
        if (!userAgent) {
            return "Unknown";
        }

        if (/Android/i.test(userAgent)) {
            return "Android Device";
        }

        if (/iPhone/i.test(userAgent)) {
            return "iPhone";
        }

        if (/iPad/i.test(userAgent)) {
            return "iPad";
        }

        if (/Windows/i.test(userAgent)) {
            return "Windows PC";
        }

        if (/Macintosh|Mac OS/i.test(userAgent)) {
            return "Mac";
        }

        if (/Linux/i.test(userAgent)) {
            return "Linux Device";
        }

        return "Unknown Device";
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-6xl mx-auto">

                {/* Header */}
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
                    <div>
                        <h1 className="text-3xl font-bold">
                            Login Activity
                        </h1>

                        <p className="text-slate-400 mt-2">
                            Review your recent login attempts and security events.
                        </p>
                    </div>

                    <button
                        onClick={() => navigate("/")}
                        className="px-5 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 transition"
                    >
                        ← Back to Home
                    </button>
                </div>

                {/* Loading */}
                {loading && (
                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center">
                        <p className="text-slate-400">
                            Loading login activity...
                        </p>
                    </div>
                )}

                {/* Error */}
                {!loading && error && (
                    <div className="bg-red-950/40 border border-red-800 rounded-xl p-6">
                        <p className="text-red-400">
                            {error}
                        </p>
                    </div>
                )}

                {/* Empty */}
                {!loading && !error && events.length === 0 && (
                    <div className="bg-slate-900 border border-slate-800 rounded-xl p-8 text-center">
                        <p className="text-slate-400">
                            No login activity found.
                        </p>
                    </div>
                )}

                {/* Login Activity */}
                {!loading && !error && events.length > 0 && (
                    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">

                        <div className="overflow-x-auto">
                            <table className="w-full text-left">
                                <thead className="bg-slate-800">
                                    <tr>
                                        <th className="px-5 py-4 text-sm font-semibold text-slate-300">
                                            Date & Time
                                        </th>

                                        <th className="px-5 py-4 text-sm font-semibold text-slate-300">
                                            Status
                                        </th>

                                        <th className="px-5 py-4 text-sm font-semibold text-slate-300">
                                            IP Address
                                        </th>

                                        <th className="px-5 py-4 text-sm font-semibold text-slate-300">
                                            Device
                                        </th>

                                        <th className="px-5 py-4 text-sm font-semibold text-slate-300">
                                            Browser
                                        </th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {events.map((event) => {
                                        const success =
                                            event.loginStatus === "SUCCESS";

                                        return (
                                            <tr
                                                key={event.id}
                                                className="border-t border-slate-800 hover:bg-slate-800/50 transition"
                                            >
                                                <td className="px-5 py-4 text-sm text-slate-300">
                                                    {formatDateTime(
                                                        event.eventTime
                                                    )}
                                                </td>

                                                <td className="px-5 py-4">
                                                    <span
                                                        className={`inline-flex px-3 py-1 rounded-full text-xs font-semibold ${
                                                            success
                                                                ? "bg-green-900/50 text-green-400"
                                                                : "bg-red-900/50 text-red-400"
                                                        }`}
                                                    >
                                                        {success
                                                            ? "SUCCESS"
                                                            : "FAILURE"}
                                                    </span>
                                                </td>

                                                <td className="px-5 py-4 text-sm text-slate-300">
                                                    {event.ipAddress || "N/A"}
                                                </td>

                                                <td className="px-5 py-4 text-sm text-slate-300">
                                                    {getDeviceName(
                                                        event.userAgent
                                                    )}
                                                </td>

                                                <td className="px-5 py-4 text-sm text-slate-300">
                                                    {getBrowserName(
                                                        event.userAgent
                                                    )}
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}

                {/* Security Information */}
                {!loading && !error && (
                    <div className="mt-6 bg-slate-900 border border-slate-800 rounded-xl p-5">
                        <h2 className="text-lg font-semibold mb-2">
                            Security Information
                        </h2>

                        <p className="text-sm text-slate-400">
                            Login attempts are recorded with the authentication
                            result, timestamp, IP address, and browser/device
                            information available from the request.
                        </p>
                    </div>
                )}
            </div>
        </div>
    );
}

export default LoginActivity;