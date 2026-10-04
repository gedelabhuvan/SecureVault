import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function TeamCredentials() {
    const [credentials, setCredentials] = useState([]);
    const [loading, setLoading] = useState(true);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const [visiblePasswords, setVisiblePasswords] =
        useState({});

    const [editingCredentialId, setEditingCredentialId] =
        useState(null);

    const [editTitle, setEditTitle] = useState("");
    const [editUsername, setEditUsername] = useState("");
    const [editPassword, setEditPassword] = useState("");

    const token = localStorage.getItem("token");

    const authHeaders = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
    };

    // =========================================================
    // LOAD TEAM CREDENTIALS
    // =========================================================

    useEffect(() => {
        loadTeamCredentials();
    }, []);

    async function loadTeamCredentials() {
        try {
            setLoading(true);
            setError("");

            if (!token) {
                throw new Error("You are not logged in.");
            }

            const response = await fetch(
                `${API_BASE_URL}/api/team-access/my-credentials`,
                {
                    method: "GET",
                    headers: authHeaders,
                }
            );

            let data = {};

            try {
                data = await response.json();
            } catch {
                // No response body
            }

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Failed to load team credentials."
                );
            }

            console.log("Team credentials:", data);

            setCredentials(
                Array.isArray(data) ? data : []
            );

        } catch (err) {
            setCredentials([]);
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    // =========================================================
    // SHOW / HIDE PASSWORD
    // =========================================================

    function togglePassword(id) {
        setVisiblePasswords((previous) => ({
            ...previous,
            [id]: !previous[id],
        }));
    }

    // =========================================================
    // PERMISSION LABEL
    // =========================================================

    function getPermissionLabel(permission) {
        switch (permission) {
            case "VIEW_ONLY":
                return "View Only";

            case "EDIT_ACCESS":
                return "Edit Access";

            case "FULL_MANAGEMENT":
                return "Full Management";

            default:
                return permission || "Unknown";
        }
    }

    // =========================================================
    // PERMISSION DESCRIPTION
    // =========================================================

    function getPermissionDescription(permission) {
        switch (permission) {
            case "VIEW_ONLY":
                return "You can view this credential but cannot modify it.";

            case "EDIT_ACCESS":
                return "You can view and update this credential.";

            case "FULL_MANAGEMENT":
                return "You can view, update, and manage access to this credential.";

            default:
                return "";
        }
    }

    // =========================================================
    // PERMISSION CSS
    // =========================================================

    function getPermissionClass(permission) {
        switch (permission) {
            case "VIEW_ONLY":
                return "border-blue-500/20 bg-blue-500/10 text-blue-400";

            case "EDIT_ACCESS":
                return "border-yellow-500/20 bg-yellow-500/10 text-yellow-400";

            case "FULL_MANAGEMENT":
                return "border-violet-500/20 bg-violet-500/10 text-violet-400";

            default:
                return "border-slate-700 bg-slate-800 text-slate-400";
        }
    }

    // =========================================================
    // CHECK EDIT PERMISSION
    // =========================================================

    function canEdit(permission) {
        return (
            permission === "EDIT_ACCESS" ||
            permission === "FULL_MANAGEMENT"
        );
    }

    // =========================================================
    // OPEN EDIT
    // =========================================================

    function openEdit(credential) {
        if (!canEdit(credential.permissionLevel)) {
            setError(
                "You have View Only permission and cannot edit this credential."
            );
            return;
        }

        setEditingCredentialId(
            credential.credentialId
        );

        setEditTitle(
            credential.title || ""
        );

        setEditUsername(
            credential.username || ""
        );

        setEditPassword(
            credential.password || ""
        );

        setMessage("");
        setError("");
    }

    // =========================================================
    // CANCEL EDIT
    // =========================================================

    function cancelEdit() {
        setEditingCredentialId(null);

        setEditTitle("");
        setEditUsername("");
        setEditPassword("");

        setMessage("");
        setError("");
    }

    // =========================================================
    // UPDATE CREDENTIAL
    // =========================================================

    async function updateCredential(
        event,
        credential
    ) {
        event.preventDefault();

        setMessage("");
        setError("");

        if (!editTitle.trim()) {
            setError("Credential title is required.");
            return;
        }

        if (!editUsername.trim()) {
            setError("Username is required.");
            return;
        }

        if (!editPassword) {
            setError("Password is required.");
            return;
        }

        try {
            const response = await fetch(
                `${API_BASE_URL}/api/team-access/teams/${credential.teamId}/credentials/${credential.credentialId}`,
                {
                    method: "PUT",
                    headers: authHeaders,
                    body: JSON.stringify({
                        title: editTitle.trim(),
                        username: editUsername.trim(),
                        password: editPassword,
                    }),
                }
            );

            let data = {};

            try {
                data = await response.json();
            } catch {
                // No response body
            }

            if (!response.ok) {
                throw new Error(
                    data.message ||
                    "Failed to update credential."
                );
            }

            setMessage(
                "Team credential updated successfully."
            );

            setEditingCredentialId(null);

            setEditTitle("");
            setEditUsername("");
            setEditPassword("");

            await loadTeamCredentials();

        } catch (err) {
            setError(err.message);
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <div className="min-h-screen bg-slate-950 px-6 py-10 text-white">

            <div className="mx-auto max-w-7xl">

                {/* HEADER */}

                <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                    <div>

                        <h1 className="text-3xl font-bold">
                            Team Credentials
                        </h1>

                        <p className="mt-2 text-slate-400">
                            Credentials shared with you through your teams.
                        </p>

                    </div>

                    <button
                        type="button"
                        onClick={loadTeamCredentials}
                        className="rounded-lg bg-slate-700 px-5 py-3 font-semibold hover:bg-slate-600"
                    >
                        Refresh
                    </button>

                </div>

                {/* SUCCESS MESSAGE */}

                {message && (
                    <div className="mb-6 rounded-lg border border-green-500/30 bg-green-500/10 p-4 text-green-400">
                        ✓ {message}
                    </div>
                )}

                {/* ERROR MESSAGE */}

                {error && (
                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-red-400">
                        ⚠ {error}
                    </div>
                )}

                {/* LOADING */}

                {loading ? (

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-10 text-center">

                        <div className="mx-auto h-10 w-10 animate-spin rounded-full border-2 border-slate-700 border-t-blue-500"></div>

                        <p className="mt-4 text-slate-400">
                            Loading team credentials...
                        </p>

                    </div>

                ) : credentials.length === 0 ? (

                    /* EMPTY */

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-10 text-center">

                        <div className="text-5xl">
                            🔐
                        </div>

                        <h2 className="mt-4 text-xl font-semibold">
                            No Team Credentials
                        </h2>

                        <p className="mt-2 text-slate-400">
                            No credentials have been shared with you through your teams.
                        </p>

                    </div>

                ) : (

                    /* CREDENTIAL CARDS */

                    <div className="grid gap-6 md:grid-cols-2">

                        {credentials.map((credential) => {

                            const credentialId =
                                credential.credentialId ??
                                credential.id;

                            const permission =
                                credential.permissionLevel;

                            const passwordVisible =
                                visiblePasswords[
                                    credentialId
                                ];

                            const isEditing =
                                editingCredentialId ===
                                credentialId;

                            return (
                                <div
                                    key={credentialId}
                                    className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg"
                                >

                                    {/* CARD HEADER */}

                                    <div className="flex items-start justify-between gap-4">

                                        <div>

                                            <h2 className="text-xl font-semibold">
                                                {credential.title}
                                            </h2>

                                            <p className="mt-1 text-sm text-slate-500">
                                                Team Credential
                                            </p>

                                        </div>

                                        <span
                                            className={`rounded-full border px-3 py-1 text-xs font-semibold ${getPermissionClass(
                                                permission
                                            )}`}
                                        >
                                            {getPermissionLabel(
                                                permission
                                            )}
                                        </span>

                                    </div>

                                    {/* PERMISSION INFO */}

                                    <div className="mt-4 rounded-lg bg-slate-800 p-3">

                                        <p className="text-xs text-slate-400">
                                            {getPermissionDescription(
                                                permission
                                            )}
                                        </p>

                                    </div>

                                    {!isEditing ? (

                                        <>

                                            {/* USERNAME */}

                                            <div className="mt-5">

                                                <label className="mb-2 block text-xs font-medium text-slate-500">
                                                    Username
                                                </label>

                                                <div className="rounded-lg bg-slate-800 px-4 py-3 text-sm text-slate-300">
                                                    {
                                                        credential.username
                                                    }
                                                </div>

                                            </div>

                                            {/* PASSWORD */}

                                            <div className="mt-4">

                                                <label className="mb-2 block text-xs font-medium text-slate-500">
                                                    Password
                                                </label>

                                                <div className="flex gap-2">

                                                    <div className="flex-1 overflow-hidden rounded-lg bg-slate-800 px-4 py-3 font-mono text-sm text-slate-300">

                                                        {passwordVisible
                                                            ? credential.password
                                                            : "••••••••••••"}

                                                    </div>

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            togglePassword(
                                                                credentialId
                                                            )
                                                        }
                                                        className="rounded-lg bg-slate-700 px-4 py-3 text-sm font-semibold hover:bg-slate-600"
                                                    >
                                                        {passwordVisible
                                                            ? "Hide"
                                                            : "Show"}
                                                    </button>

                                                </div>

                                            </div>

                                            {/* TEAM */}

                                            {credential.teamName && (

                                                <div className="mt-4">

                                                    <label className="mb-2 block text-xs font-medium text-slate-500">
                                                        Team
                                                    </label>

                                                    <div className="rounded-lg bg-slate-800 px-4 py-3 text-sm text-slate-300">
                                                        {
                                                            credential.teamName
                                                        }
                                                    </div>

                                                </div>

                                            )}

                                            {/* EDIT BUTTON */}

                                            <div className="mt-5">

                                                {canEdit(
                                                    permission
                                                ) ? (

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            openEdit(
                                                                credential
                                                            )
                                                        }
                                                        className="w-full rounded-lg bg-blue-600 px-4 py-3 text-sm font-semibold hover:bg-blue-700"
                                                    >
                                                        ✏️ Edit Credential
                                                    </button>

                                                ) : (

                                                    <div className="rounded-lg border border-slate-700 bg-slate-800 px-4 py-3 text-center text-xs text-slate-500">
                                                        View Only — editing is disabled
                                                    </div>

                                                )}

                                            </div>

                                        </>

                                    ) : (

                                        /* EDIT FORM */

                                        <form
                                            onSubmit={(event) =>
                                                updateCredential(
                                                    event,
                                                    credential
                                                )
                                            }
                                            className="mt-5 space-y-4"
                                        >

                                            <div>

                                                <label className="mb-2 block text-sm text-slate-300">
                                                    Title
                                                </label>

                                                <input
                                                    type="text"
                                                    value={editTitle}
                                                    onChange={(event) =>
                                                        setEditTitle(
                                                            event
                                                                .target
                                                                .value
                                                        )
                                                    }
                                                    className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                                />

                                            </div>

                                            <div>

                                                <label className="mb-2 block text-sm text-slate-300">
                                                    Username
                                                </label>

                                                <input
                                                    type="text"
                                                    value={
                                                        editUsername
                                                    }
                                                    onChange={(event) =>
                                                        setEditUsername(
                                                            event
                                                                .target
                                                                .value
                                                        )
                                                    }
                                                    className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                                />

                                            </div>

                                            <div>

                                                <label className="mb-2 block text-sm text-slate-300">
                                                    Password
                                                </label>

                                                <input
                                                    type="text"
                                                    value={
                                                        editPassword
                                                    }
                                                    onChange={(event) =>
                                                        setEditPassword(
                                                            event
                                                                .target
                                                                .value
                                                        )
                                                    }
                                                    className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 font-mono text-white outline-none focus:border-blue-500"
                                                />

                                            </div>

                                            <div className="flex gap-3">

                                                <button
                                                    type="submit"
                                                    className="flex-1 rounded-lg bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-700"
                                                >
                                                    Save Changes
                                                </button>

                                                <button
                                                    type="button"
                                                    onClick={
                                                        cancelEdit
                                                    }
                                                    className="rounded-lg bg-slate-700 px-5 py-3 font-semibold hover:bg-slate-600"
                                                >
                                                    Cancel
                                                </button>

                                            </div>

                                        </form>

                                    )}

                                </div>
                            );
                        })}

                    </div>

                )}

            </div>

        </div>
    );
}

export default TeamCredentials;