import { useEffect, useState } from "react";

function SecurityAlerts() {
    const [alerts, setAlerts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    const fetchAlerts = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/security/alerts",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load security alerts"
                );
            }

            setAlerts(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAlerts();
    }, []);

    const handleResolve = async (alertId) => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/security/alerts/${alertId}/resolve`,
                {
                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to resolve alert");
            }

            fetchAlerts();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-5xl mx-auto">
                <h1 className="text-3xl font-bold mb-2">
                    🚨 Security Alerts
                </h1>

                <p className="text-slate-400 mb-6">
                    Monitor and manage security alerts.
                </p>

                {loading && (
                    <p className="text-slate-400">
                        Loading alerts...
                    </p>
                )}

                {error && (
                    <div className="mb-4 p-4 rounded-xl bg-red-950/40 border border-red-800 text-red-300">
                        {error}
                    </div>
                )}

                {!loading && !error && alerts.length === 0 && (
                    <div className="p-8 text-center rounded-xl bg-slate-900 border border-slate-800">
                        <p className="text-slate-400">
                            No security alerts found.
                        </p>
                    </div>
                )}

                <div className="space-y-4">
                    {alerts.map((alert) => (
                        <div
                            key={alert.id}
                            className="p-5 rounded-xl bg-slate-900 border border-slate-800"
                        >
                            <div className="flex items-center justify-between gap-4">
                                <div>
                                    <h2 className="text-lg font-semibold">
                                        {alert.alertType || "Security Alert"}
                                    </h2>

                                    <p className="text-slate-400 mt-2">
                                        {alert.message}
                                    </p>

                                    <p className="text-sm text-slate-500 mt-2">
                                        Status: {alert.status}
                                    </p>
                                </div>

                                {alert.status === "ACTIVE" && (
                                    <button
                                        type="button"
                                        onClick={() =>
                                            handleResolve(alert.id)
                                        }
                                        className="px-4 py-2 rounded-lg bg-cyan-600 hover:bg-cyan-500"
                                    >
                                        Resolve
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    );
}

export default SecurityAlerts;