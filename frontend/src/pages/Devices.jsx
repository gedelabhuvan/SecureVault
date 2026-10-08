import React, { useEffect, useState } from "react";

function Devices() {
    const [devices, setDevices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const token = localStorage.getItem("token");

    const fetchDevices = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/security/devices",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load devices"
                );
            }

            setDevices(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchDevices();
    }, []);

    const updateTrust = async (deviceId, trusted) => {
        try {
            const response = await fetch(
                `http://localhost:8080/api/security/devices/${deviceId}/${trusted ? "trust" : "untrust"}`,
                {
                    method: "PUT",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to update device");
            }

            fetchDevices();
        } catch (err) {
            setError(err.message);
        }
    };

    const removeDevice = async (deviceId) => {
        if (!window.confirm("Remove this device?")) return;

        try {
            const response = await fetch(
                `http://localhost:8080/api/security/devices/${deviceId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to remove device");
            }

            fetchDevices();
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-5xl mx-auto">
                <h1 className="text-3xl font-bold mb-2">
                    📱 Device Management
                </h1>

                <p className="text-slate-400 mb-8">
                    View and manage devices connected to your SecureVault account.
                </p>

                {error && (
                    <div className="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-red-300">
                        {error}
                    </div>
                )}

                {loading ? (
                    <p className="text-slate-400">
                        Loading devices...
                    </p>
                ) : devices.length === 0 ? (
                    <div className="rounded-2xl border border-slate-800 bg-slate-900 p-8 text-center">
                        <p className="text-slate-400">
                            No devices found.
                        </p>
                    </div>
                ) : (
                    <div className="grid gap-5 md:grid-cols-2">
                        {devices.map((device) => (
                            <div
                                key={device.id}
                                className="rounded-2xl border border-slate-800 bg-slate-900 p-6 shadow-xl"
                            >
                                <div className="flex items-center justify-between mb-5">
                                    <div>
                                        <h2 className="text-xl font-semibold">
                                            {device.deviceName ||
                                                "Unknown Device"}
                                        </h2>

                                        <p className="text-sm text-slate-400">
                                            {device.browser ||
                                                "Unknown Browser"}
                                        </p>
                                    </div>

                                    <span
                                        className={`px-3 py-1 rounded-full text-xs font-semibold ${
                                            device.trusted
                                                ? "bg-green-500/20 text-green-300"
                                                : "bg-yellow-500/20 text-yellow-300"
                                        }`}
                                    >
                                        {device.trusted
                                            ? "Trusted"
                                            : "Untrusted"}
                                    </span>
                                </div>

                                <div className="space-y-2 text-sm text-slate-400 mb-6">
                                    <p>
                                        <span className="text-slate-300">
                                            Operating System:
                                        </span>{" "}
                                        {device.operatingSystem ||
                                            "Unknown"}
                                    </p>

                                    <p>
                                        <span className="text-slate-300">
                                            IP Address:
                                        </span>{" "}
                                        {device.ipAddress || "Unknown"}
                                    </p>

                                    <p>
                                        <span className="text-slate-300">
                                            Login Count:
                                        </span>{" "}
                                        {device.loginCount ?? 0}
                                    </p>
                                </div>

                                <div className="flex flex-wrap gap-3">
                                    <button
                                        onClick={() =>
                                            updateTrust(
                                                device.id,
                                                !device.trusted
                                            )
                                        }
                                        className="flex-1 rounded-xl bg-cyan-600 px-4 py-3 font-semibold hover:bg-cyan-500 transition"
                                    >
                                        {device.trusted
                                            ? "Untrust Device"
                                            : "Trust Device"}
                                    </button>

                                    <button
                                        onClick={() =>
                                            removeDevice(device.id)
                                        }
                                        className="rounded-xl bg-red-600 px-4 py-3 font-semibold hover:bg-red-500 transition"
                                    >
                                        Remove
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    );
}

export default Devices;