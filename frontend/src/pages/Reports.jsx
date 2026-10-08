import React, { useEffect, useState } from "react";

function Reports() {
    const [credentials, setCredentials] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    useEffect(() => {
        fetchReport();
    }, []);

    const fetchReport = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/reports/credentials",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load report"
                );
            }

            setCredentials(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    const downloadCSV = async () => {
        try {
            const response = await fetch(
                "http://localhost:8080/api/reports/credentials/export",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (!response.ok) {
                throw new Error("Failed to download CSV");
            }

            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);

            const link = document.createElement("a");
            link.href = url;
            link.download = "securevault-credentials.csv";
            document.body.appendChild(link);
            link.click();

            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (err) {
            setError(err.message);
        }
    };

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-6xl mx-auto">

                <h1 className="text-3xl font-bold text-cyan-400">
                    📊 Reports
                </h1>

                <p className="mt-2 text-slate-400">
                    View your credential report and export your data.
                </p>

                {error && (
                    <div className="mt-6 p-4 rounded-xl border border-red-500/40 bg-red-500/10 text-red-400">
                        {error}
                    </div>
                )}

                <div className="mt-8 flex flex-wrap gap-4">
                    <button
                        onClick={fetchReport}
                        className="px-5 py-3 rounded-xl bg-slate-800 hover:bg-slate-700"
                    >
                        🔄 Refresh Report
                    </button>

                    <button
                        onClick={downloadCSV}
                        className="px-5 py-3 rounded-xl bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-semibold"
                    >
                        📥 Download CSV
                    </button>
                </div>

                <div className="mt-8 rounded-2xl border border-slate-800 bg-slate-900 p-6">

                    <h2 className="text-xl font-semibold mb-5">
                        Credential Report
                    </h2>

                    {loading ? (
                        <p className="text-slate-400">
                            Loading report...
                        </p>
                    ) : credentials.length === 0 ? (
                        <p className="text-slate-400">
                            No credentials available.
                        </p>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-left">
                                <thead>
                                    <tr className="border-b border-slate-700">
                                        <th className="p-3">ID</th>
                                        <th className="p-3">Title</th>
                                        <th className="p-3">Username</th>
                                        <th className="p-3">Favorite</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    {credentials.map((credential) => (
                                        <tr
                                            key={credential.id}
                                            className="border-b border-slate-800"
                                        >
                                            <td className="p-3">
                                                {credential.id}
                                            </td>

                                            <td className="p-3">
                                                {credential.title}
                                            </td>

                                            <td className="p-3">
                                                {credential.username}
                                            </td>

                                            <td className="p-3">
                                                {credential.favorite
                                                    ? "⭐ Yes"
                                                    : "No"}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default Reports;