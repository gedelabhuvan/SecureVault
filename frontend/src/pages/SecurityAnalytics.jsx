import React, { useEffect, useState } from "react";

function SecurityAnalytics() {

    const [analytics, setAnalytics] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [filter, setFilter] = useState("7days");
    const [fromDate, setFromDate] = useState("");
    const [toDate, setToDate] = useState("");

    useEffect(() => {
        fetchAnalytics();
    }, []);

    async function fetchAnalytics(selectedFilter = filter) {

    try {

        setLoading(true);
        setError("");

        const token = localStorage.getItem("token");

        if (!token) {
            setError("Please login to view security analytics.");
            setLoading(false);
            return;
        }

        let url =
            "http://localhost:8080/api/security/analytics";

        // TODAY
        if (selectedFilter === "today") {

            const today = new Date()
                .toISOString()
                .split("T")[0];

            url += `?from=${today}&to=${today}`;
        }

        // LAST 7 DAYS
        else if (selectedFilter === "7days") {

            const today = new Date();

            const sevenDaysAgo = new Date();
            sevenDaysAgo.setDate(
                today.getDate() - 6
            );

            const from =
                sevenDaysAgo
                    .toISOString()
                    .split("T")[0];

            const to =
                today
                    .toISOString()
                    .split("T")[0];

            url += `?from=${from}&to=${to}`;
        }

        // CUSTOM DATE RANGE
        else if (
            selectedFilter === "custom" &&
            fromDate &&
            toDate
        ) {

            url += `?from=${fromDate}&to=${toDate}`;
        }

        const response = await fetch(
            url,
            {
                method: "GET",
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            }
        );

        if (!response.ok) {
            throw new Error(
                `Failed to load analytics (${response.status})`
            );
        }

        const data = await response.json();

        setAnalytics(data);

    } catch (err) {

        console.error(
            "Analytics error:",
            err
        );

        setError(
            "Unable to load security analytics. Please try again."
        );

    } finally {

        setLoading(false);

    }
}

    // =========================================
    // LOADING
    // =========================================

    if (loading) {

        return (
            <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">

                <p className="text-cyan-400 text-lg">
                    Loading security analytics...
                </p>

            </div>
        );
    }

    // =========================================
    // ERROR
    // =========================================

    if (error) {

        return (
            <div className="min-h-screen bg-slate-950 text-white flex items-center justify-center">

                <div className="rounded-xl border border-red-500/30 bg-red-500/10 p-8 text-center">

                    <h2 className="text-xl font-bold text-red-400">
                        Security Analytics
                    </h2>

                    <p className="mt-3 text-slate-300">
                        {error}
                    </p>

                    <button
                        onClick={fetchAnalytics}
                        className="mt-5 rounded-lg bg-cyan-600 px-5 py-2 font-semibold hover:bg-cyan-500"
                    >
                        Retry
                    </button>

                </div>

            </div>
        );
    }

    // =========================================
    // EMPTY DATA SAFETY
    // =========================================

    const successfulLogins =
        analytics?.successfulLogins ?? 0;

    const failedLogins =
        analytics?.failedLogins ?? 0;

    const totalLogins =
        analytics?.totalLogins ?? 0;

    const successRate =
        analytics?.loginSuccessRate ?? 0;

   const failureRate =
    analytics?.loginFailureRate ?? 0;

const suspiciousActivities =
    analytics?.suspiciousActivities ?? 0;
    const activeSecurityAlerts =
    analytics?.activeSecurityAlerts ?? 0;

const recentActivity =
    analytics?.recentActivity ?? [];

    // =========================================
    // MAIN DASHBOARD
    // =========================================

    return (
        <div className="min-h-screen bg-slate-950 text-white">

            {/* HEADER */}

            <header className="border-b border-slate-800">

                <div className="mx-auto max-w-7xl px-6 py-6">

                    <h1 className="text-3xl font-bold text-cyan-400">
                        Security Analytics
                    </h1>

                    <p className="mt-2 text-slate-400">
                        Monitor your account login security.
                    </p>

                </div>

            </header>


            <main className="mx-auto max-w-7xl px-6 py-10">

                {/* =================================
                    METRIC CARDS
                   ================================= */}

                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-7">

                    {/* Successful */}

                    <div className="rounded-xl border border-green-500/20 bg-slate-900 p-6">

                        <p className="text-sm text-slate-400">
                            Successful Logins
                        </p>

                        <p className="mt-3 text-4xl font-bold text-green-400">
                            {successfulLogins}
                        </p>

                    </div>


                    {/* Failed */}

                    <div className="rounded-xl border border-red-500/20 bg-slate-900 p-6">

                        <p className="text-sm text-slate-400">
                            Failed Logins
                        </p>

                        <p className="mt-3 text-4xl font-bold text-red-400">
                            {failedLogins}
                        </p>

                    </div>


                    {/* Total */}

                    <div className="rounded-xl border border-blue-500/20 bg-slate-900 p-6">

                        <p className="text-sm text-slate-400">
                            Total Logins
                        </p>

                        <p className="mt-3 text-4xl font-bold text-blue-400">
                            {totalLogins}
                        </p>

                    </div>


                    {/* Success Rate */}

                    <div className="rounded-xl border border-cyan-500/20 bg-slate-900 p-6">

                        <p className="text-sm text-slate-400">
                            Success Rate
                        </p>

                        <p className="mt-3 text-4xl font-bold text-cyan-400">
                            {successRate.toFixed(1)}%
                        </p>

                    </div>


                    {/* Failure Rate */}

                    <div className="rounded-xl border border-yellow-500/20 bg-slate-900 p-6">

                        <p className="text-sm text-slate-400">
                            Failure Rate
                        </p>

                        <p className="mt-3 text-4xl font-bold text-yellow-400">
                            {failureRate.toFixed(1)}%
                        </p>

                    </div>
                    {/* Suspicious Activities */}

<div className="rounded-xl border border-orange-500/20 bg-slate-900 p-6">

    <p className="text-sm text-slate-400">
        Suspicious Activities
    </p>

    <p className="mt-3 text-4xl font-bold text-orange-400">
        {suspiciousActivities}
    </p>

</div>

                {/* Active Security Alerts */}

                <div className="rounded-xl border border-purple-500/20 bg-slate-900 p-6">

                    <p className="text-sm text-slate-400">
                        Active Security Alerts
                    </p>

                    <p className="mt-3 text-4xl font-bold text-purple-400">
                        {activeSecurityAlerts}
                    </p>

                </div>

                </div>


                {/* =================================
    TIME FILTER
   ================================= */}

<div className="mt-8 rounded-xl border border-slate-800 bg-slate-900 p-6">

    <h2 className="text-xl font-bold">
        Time Filter
    </h2>

    <div className="mt-5 flex flex-wrap gap-3">

        {/* TODAY */}

        <button
            onClick={() => {
                setFilter("today");
                fetchAnalytics("today");
            }}
            className={`rounded-lg px-5 py-2 font-semibold ${
                filter === "today"
                    ? "bg-cyan-600 text-white"
                    : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
        >
            Today
        </button>


        {/* LAST 7 DAYS */}

        <button
            onClick={() => {
                setFilter("7days");
                fetchAnalytics("7days");
            }}
            className={`rounded-lg px-5 py-2 font-semibold ${
                filter === "7days"
                    ? "bg-cyan-600 text-white"
                    : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
        >
            Last 7 Days
        </button>


        {/* CUSTOM */}

        <button
            onClick={() => {
                setFilter("custom");
            }}
            className={`rounded-lg px-5 py-2 font-semibold ${
                filter === "custom"
                    ? "bg-cyan-600 text-white"
                    : "bg-slate-800 text-slate-300 hover:bg-slate-700"
            }`}
        >
            Custom Range
        </button>

    </div>


    {/* CUSTOM DATE INPUTS */}

    {filter === "custom" && (

        <div className="mt-5 grid gap-4 md:grid-cols-3">

            <div>

                <label className="text-sm text-slate-400">
                    From Date
                </label>

                <input
                    type="date"
                    value={fromDate}
                    onChange={(e) =>
                        setFromDate(e.target.value)
                    }
                    className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-2 text-white"
                />

            </div>


            <div>

                <label className="text-sm text-slate-400">
                    To Date
                </label>

                <input
                    type="date"
                    value={toDate}
                    onChange={(e) =>
                        setToDate(e.target.value)
                    }
                    className="mt-2 w-full rounded-lg border border-slate-700 bg-slate-950 px-4 py-2 text-white"
                />

            </div>


            <div className="flex items-end">

                <button
                    onClick={() =>
                        fetchAnalytics("custom")
                    }
                    disabled={
                        !fromDate || !toDate
                    }
                    className="w-full rounded-lg bg-cyan-600 px-5 py-2 font-semibold text-white hover:bg-cyan-500 disabled:cursor-not-allowed disabled:opacity-50"
                >
                    Apply Filter
                </button>

            </div>

        </div>

    )}


    {/* CURRENT ANALYTICS PERIOD */}

    <div className="mt-6 border-t border-slate-800 pt-5">

        <h3 className="text-lg font-semibold">
            Analytics Period
        </h3>

        <div className="mt-4 grid gap-4 md:grid-cols-2">

            <div>

                <p className="text-sm text-slate-400">
                    From
                </p>

                <p className="mt-1 text-slate-200">
                    {analytics?.from
                        ? new Date(
                            analytics.from
                        ).toLocaleString()
                        : "No data"}
                </p>

            </div>


            <div>

                <p className="text-sm text-slate-400">
                    To
                </p>

                <p className="mt-1 text-slate-200">
                    {analytics?.to
                        ? new Date(
                            analytics.to
                        ).toLocaleString()
                        : "No data"}
                </p>

            </div>

        </div>

    </div>

</div>


                {/* =================================
                    RECENT ACTIVITY
                   ================================= */}

                <div className="mt-8 rounded-xl border border-slate-800 bg-slate-900 p-6">

                    <h2 className="text-2xl font-bold">
                        Recent Security Activity
                    </h2>

                    {recentActivity.length === 0 ? (

                        <div className="mt-6 rounded-lg border border-slate-700 bg-slate-950 p-6 text-center">

                            <p className="text-slate-400">
                                No recent security activity.
                            </p>

                        </div>

                    ) : (

                        <div className="mt-6 overflow-x-auto">

                            <table className="w-full text-left">

                                <thead>

                                    <tr className="border-b border-slate-700 text-sm text-slate-400">

                                        <th className="px-4 py-3">
                                            Date & Time
                                        </th>

                                        <th className="px-4 py-3">
                                            Event
                                        </th>

                                        <th className="px-4 py-3">
                                            Status
                                        </th>

                                        <th className="px-4 py-3">
                                            IP Address
                                        </th>

                                        <th className="px-4 py-3">
                                            Device / Browser
                                        </th>

                                    </tr>

                                </thead>


                                <tbody>

                                    {recentActivity.map(
                                        (activity, index) => (

                                            <tr
                                                key={
                                                    activity.id ??
                                                    index
                                                }
                                                className="border-b border-slate-800"
                                            >

                                                <td className="px-4 py-4 text-sm text-slate-300">

                                                    {activity.eventTime
                                                        ? new Date(
                                                            activity.eventTime
                                                        ).toLocaleString()
                                                        : "-"}
                                                </td>


                                                <td className="px-4 py-4">

                                                    {activity.eventType ??
                                                        "-"}

                                                </td>


                                                <td
                                                    className={`px-4 py-4 font-semibold ${
                                                        activity.loginStatus ===
                                                        "SUCCESS"
                                                            ? "text-green-400"
                                                            : "text-red-400"
                                                    }`}
                                                >

                                                    {activity.loginStatus ??
                                                        "-"}

                                                </td>


                                                <td className="px-4 py-4 text-sm text-slate-300">

                                                    {activity.ipAddress ??
                                                        "-"}

                                                </td>


                                                <td className="px-4 py-4 text-sm text-slate-400">

                                                    {activity.userAgent ??
                                                        "-"}

                                                </td>

                                            </tr>

                                        )
                                    )}

                                </tbody>

                            </table>

                        </div>

                    )}

                </div>

            </main>

        </div>
    );
}

export default SecurityAnalytics;