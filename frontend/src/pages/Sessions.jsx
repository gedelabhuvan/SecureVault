import { useEffect, useState } from "react";

function Sessions() {
    const [sessions, setSessions] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const token = localStorage.getItem("token");

    const fetchSessions = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/sessions",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load sessions"
                );
            }

            setSessions(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (!token) {
            setError("Please login first.");
            setLoading(false);
            return;
        }

        fetchSessions();
    }, []);

    const revokeSession = async (sessionId) => {
        if (!window.confirm("Revoke this session?")) {
            return;
        }

        try {
            const response = await fetch(
                `http://localhost:8080/api/sessions/${sessionId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to revoke session"
                );
            }

            fetchSessions();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white px-6 py-10">
            <div className="max-w-6xl mx-auto">

                <div className="mb-8">
                    <h1 className="text-3xl font-bold">
                        🔐 Active Sessions
                    </h1>

                    <p className="mt-2 text-slate-400">
                        View and manage devices currently signed in
                        to your SecureVault account.
                    </p>
                </div>

                {error && (
                    <div className="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 px-5 py-4 text-red-300">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 text-center text-slate-400">
                        Loading sessions...
                    </div>
                ) : sessions.length === 0 ? (
                    <div className="rounded-2xl bg-slate-900 border border-slate-800 p-8 text-center text-slate-400">
                        No active sessions found.
                    </div>
                ) : (
                    <div className="grid gap-6 md:grid-cols-2">
                        {sessions.map((session) => (
                            <div
                                key={session.id}
                                className="rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-xl"
                            >
                                <div className="flex items-center justify-between mb-5">
                                    <div className="text-3xl">
                                        💻
                                    </div>

                                    <span
                                        className={`rounded-full px-3 py-1 text-xs font-semibold ${
                                            session.revoked
                                                ? "bg-red-500/20 text-red-300"
                                                : "bg-green-500/20 text-green-300"
                                        }`}
                                    >
                                        {session.revoked
                                            ? "Revoked"
                                            : "Active"}
                                    </span>
                                </div>

                                <h2 className="text-xl font-semibold mb-4">
                                    Session #{session.id}
                                </h2>

                                <div className="space-y-3 text-sm">
                                    <div>
                                        <span className="text-slate-500">
                                            IP Address
                                        </span>
                                        <p className="text-slate-200">
                                            {session.ipAddress || "Unknown"}
                                        </p>
                                    </div>

                                    <div>
                                        <span className="text-slate-500">
                                            Browser / Device
                                        </span>
                                        <p className="text-slate-200 break-words">
                                            {session.userAgent || "Unknown"}
                                        </p>
                                    </div>

                                    <div>
                                        <span className="text-slate-500">
                                            Created
                                        </span>
                                        <p className="text-slate-200">
                                            {session.createdAt || "Unknown"}
                                        </p>
                                    </div>

                                    <div>
                                        <span className="text-slate-500">
                                            Expires
                                        </span>
                                        <p className="text-slate-200">
                                            {session.expiresAt || "Unknown"}
                                        </p>
                                    </div>
                                </div>

                                {!session.revoked && (
                                    <button
                                        onClick={() =>
                                            revokeSession(session.id)
                                        }
                                        className="mt-6 w-full rounded-xl bg-red-600 px-5 py-3 font-semibold transition hover:bg-red-500"
                                    >
                                        🚫 Revoke Session
                                    </button>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Sessions;