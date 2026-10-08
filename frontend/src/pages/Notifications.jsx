import { useEffect, useState } from "react";

function Notifications() {
    const [notifications, setNotifications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    const fetchNotifications = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/notifications",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load notifications"
                );
            }

            setNotifications(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const markAsRead = async (id) => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/notifications/${id}/read`,
                {
                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to mark notification as read");
            }

            fetchNotifications();
        } catch (err) {
            setError(err.message);
        }
    };

    useEffect(() => {
        fetchNotifications();
    }, []);

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-5xl mx-auto">

                <h1 className="text-3xl font-bold mb-2">
                    🔔 Notifications
                </h1>

                <p className="text-slate-400 mb-8">
                    View important security, login, sharing, and account notifications.
                </p>

                {error && (
                    <div className="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-red-300">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="rounded-2xl bg-slate-900 p-8 text-center text-slate-400">
                        Loading notifications...
                    </div>
                ) : notifications.length === 0 ? (
                    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-10 text-center">
                        <div className="text-5xl mb-4">🔔</div>
                        <h2 className="text-xl font-semibold">
                            No notifications
                        </h2>
                        <p className="mt-2 text-slate-400">
                            You're all caught up.
                        </p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {notifications.map((notification) => (
                            <div
                                key={notification.id}
                                className={`rounded-2xl border p-5 ${
                                    notification.read
                                        ? "border-slate-800 bg-slate-900"
                                        : "border-cyan-500/40 bg-slate-900/90"
                                }`}
                            >
                                <div className="flex items-start justify-between gap-4">

                                    <div>
                                        <h3 className="text-lg font-semibold">
                                            {notification.title ||
                                                notification.type ||
                                                "Notification"}
                                        </h3>

                                        <p className="mt-2 text-slate-400">
                                            {notification.message}
                                        </p>

                                        {notification.createdAt && (
                                            <p className="mt-3 text-xs text-slate-500">
                                                {new Date(
                                                    notification.createdAt
                                                ).toLocaleString()}
                                            </p>
                                        )}
                                    </div>

                                    {!notification.read && (
                                        <button
                                            onClick={() =>
                                                markAsRead(notification.id)
                                            }
                                            className="shrink-0 rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold hover:bg-cyan-500"
                                        >
                                            Mark as Read
                                        </button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Notifications;