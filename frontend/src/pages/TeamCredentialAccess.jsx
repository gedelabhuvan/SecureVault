import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function TeamCredentialAccess() {
    const [teams, setTeams] = useState([]);
    const [members, setMembers] = useState([]);
    const [credentials, setCredentials] = useState([]);
    const [accessList, setAccessList] = useState([]);

    const [selectedTeam, setSelectedTeam] = useState("");
    const [selectedMember, setSelectedMember] = useState("");
    const [selectedCredential, setSelectedCredential] = useState("");

    const [permissionLevel, setPermissionLevel] =
        useState("VIEW_ONLY");

    const [expirationDate, setExpirationDate] = useState("");

    const [loading, setLoading] = useState(false);
    const [memberLoading, setMemberLoading] = useState(false);
    const [credentialLoading, setCredentialLoading] = useState(false);
    const [accessLoading, setAccessLoading] = useState(false);

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    const authHeaders = {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
    };

    // =========================================================
    // INITIAL LOAD
    // =========================================================

    useEffect(() => {
        if (!token) {
            setError("You are not logged in.");
            return;
        }

        loadTeams();
        loadCredentials();
    }, []);

    // =========================================================
    // LOAD TEAMS
    // =========================================================

    async function loadTeams() {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/teams`,
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
                    data.message || "Failed to load teams."
                );
            }

            setTeams(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    // =========================================================
    // LOAD CREDENTIALS
    // =========================================================

    async function loadCredentials() {
        try {
            setCredentialLoading(true);

            const response = await fetch(
                `${API_BASE_URL}/api/vault/credentials`,
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
                    "Failed to load credentials."
                );
            }

            setCredentials(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {
            setError(err.message);
        } finally {
            setCredentialLoading(false);
        }
    }

    // =========================================================
    // LOAD TEAM MEMBERS
    // =========================================================

    async function loadMembers(teamId) {
        if (!teamId) {
            setMembers([]);
            return;
        }

        try {
            setMemberLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/teams/${teamId}/members`,
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
                    "Failed to load team members."
                );
            }

            console.log(
                "Team members:",
                data
            );

            setMembers(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {
            setMembers([]);
            setError(err.message);
        } finally {
            setMemberLoading(false);
        }
    }

    // =========================================================
    // LOAD CREDENTIAL ACCESS
    // =========================================================

    async function loadAccess(
        teamId,
        credentialId
    ) {
        if (!teamId || !credentialId) {
            setAccessList([]);
            return;
        }

        try {
            setAccessLoading(true);
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/team-access/teams/${teamId}/credentials/${credentialId}/access`,
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
                    "Failed to load credential access."
                );
            }

            setAccessList(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {
            setAccessList([]);
            setError(err.message);
        } finally {
            setAccessLoading(false);
        }
    }

    // =========================================================
    // TEAM CHANGE
    // =========================================================

    async function handleTeamChange(event) {
        const teamId = event.target.value;

        setSelectedTeam(teamId);
        setSelectedMember("");
        setAccessList([]);
        setMessage("");
        setError("");

        if (teamId) {
            await loadMembers(teamId);
        } else {
            setMembers([]);
        }
    }

    // =========================================================
    // MEMBER CHANGE
    // =========================================================

    function handleMemberChange(event) {
        setSelectedMember(
            event.target.value
        );

        setMessage("");
        setError("");
    }

    // =========================================================
    // CREDENTIAL CHANGE
    // =========================================================

    async function handleCredentialChange(event) {
        const credentialId =
            event.target.value;

        setSelectedCredential(
            credentialId
        );

        setMessage("");
        setError("");

        if (
            selectedTeam &&
            credentialId
        ) {
            await loadAccess(
                selectedTeam,
                credentialId
            );
        } else {
            setAccessList([]);
        }
    }

    // =========================================================
    // GRANT ACCESS
    // =========================================================

    async function grantAccess(event) {
        event.preventDefault();

        setMessage("");
        setError("");

        if (!selectedTeam) {
            setError(
                "Select a team."
            );
            return;
        }

        if (!selectedMember) {
            setError(
                "Select a team member."
            );
            return;
        }

        if (!selectedCredential) {
            setError(
                "Select a credential."
            );
            return;
        }

        try {
            setLoading(true);

            const requestBody = {
                userId: Number(
                    selectedMember
                ),

                permissionLevel:
                    permissionLevel,

                expirationDate:
                    expirationDate
                        ? expirationDate
                        : null,
            };

            console.log(
                "Grant access request:",
                requestBody
            );

            const response = await fetch(
                `${API_BASE_URL}/api/team-access/teams/${selectedTeam}/credentials/${selectedCredential}`,
                {
                    method: "POST",
                    headers: authHeaders,
                    body: JSON.stringify(
                        requestBody
                    ),
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
                    "Failed to grant team credential access."
                );
            }

            setMessage(
                "Credential access granted successfully."
            );

            setExpirationDate("");

            await loadAccess(
                selectedTeam,
                selectedCredential
            );

        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    }

    // =========================================================
    // REMOVE ACCESS
    // =========================================================

    async function removeAccess(userId) {
        if (
            !selectedTeam ||
            !selectedCredential ||
            !userId
        ) {
            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to remove this user's credential access?"
            );

        if (!confirmed) {
            return;
        }

        try {
            setMessage("");
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/team-access/teams/${selectedTeam}/credentials/${selectedCredential}/users/${userId}`,
                {
                    method: "DELETE",
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
                    "Failed to remove access."
                );
            }

            setMessage(
                "Credential access removed successfully."
            );

            await loadAccess(
                selectedTeam,
                selectedCredential
            );

        } catch (err) {
            setError(err.message);
        }
    }

    // =========================================================
    // FORMAT EXPIRATION DATE
    // =========================================================

    function formatExpirationDate(expirationDate) {
        if (!expirationDate) {
            return "No expiration";
        }

        const date = new Date(expirationDate);

        if (Number.isNaN(date.getTime())) {
            return expirationDate;
        }

        return date.toLocaleString("en-IN", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        });
    }

    // =========================================================
    // PERMISSION LABEL
    // =========================================================

    function getPermissionLabel(
        permission
    ) {
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

    function getPermissionDescription(
        permission
    ) {
        switch (permission) {
            case "VIEW_ONLY":
                return "Can view the credential but cannot modify it.";

            case "EDIT_ACCESS":
                return "Can view and update the credential.";

            case "FULL_MANAGEMENT":
                return "Can view, update, and manage credential access.";

            default:
                return "";
        }
    }

    // =========================================================
    // PERMISSION CSS
    // =========================================================

    function getPermissionClass(
        permission
    ) {
        switch (permission) {
            case "VIEW_ONLY":
                return "bg-blue-500/10 text-blue-400 border-blue-500/20";

            case "EDIT_ACCESS":
                return "bg-yellow-500/10 text-yellow-400 border-yellow-500/20";

            case "FULL_MANAGEMENT":
                return "bg-violet-500/10 text-violet-400 border-violet-500/20";

            default:
                return "bg-slate-800 text-slate-400 border-slate-700";
        }
    }

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <div className="min-h-screen bg-slate-950 px-6 py-10 text-white">

            <div className="mx-auto max-w-6xl">

                {/* HEADER */}

                <div className="mb-8">

                    <h1 className="text-3xl font-bold">
                        Team Credential Access
                    </h1>

                    <p className="mt-2 text-slate-400">
                        Control which team members can access your credentials.
                    </p>

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

                {/* =====================================================
                    GRANT ACCESS
                ====================================================== */}

                <div className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                    <h2 className="mb-6 text-xl font-semibold">
                        Grant Team Access
                    </h2>

                    <form
                        onSubmit={grantAccess}
                        className="grid gap-5 md:grid-cols-2"
                    >

                        {/* TEAM */}

                        <div>

                            <label className="mb-2 block text-sm text-slate-300">
                                Team
                            </label>

                            <select
                                value={selectedTeam}
                                onChange={
                                    handleTeamChange
                                }
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                            >

                                <option value="">
                                    Select a team
                                </option>

                                {teams.map(
                                    (team) => (
                                        <option
                                            key={
                                                team.id
                                            }
                                            value={
                                                team.id
                                            }
                                        >
                                            {team.name}
                                        </option>
                                    )
                                )}

                            </select>

                        </div>

                        {/* TEAM MEMBER */}

                        <div>

                            <label className="mb-2 block text-sm text-slate-300">
                                Team Member
                            </label>

                            <select
                                value={
                                    selectedMember
                                }
                                onChange={
                                    handleMemberChange
                                }
                                disabled={
                                    !selectedTeam ||
                                    memberLoading
                                }
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
                            >

                                <option value="">
                                    {memberLoading
                                        ? "Loading members..."
                                        : "Select a team member"}
                                </option>

                                {members
                                    .filter(
                                        (member) =>
                                            member.role !==
                                            "OWNER"
                                    )
                                    .map(
                                        (member) => (
                                            <option
                                                key={
                                                    member.userId
                                                }
                                                value={
                                                    member.userId
                                                }
                                            >
                                                {
                                                    member.username
                                                }{" "}
                                                —{" "}
                                                {
                                                    member.email
                                                }
                                            </option>
                                        )
                                    )}

                            </select>

                            {!selectedTeam && (
                                <p className="mt-2 text-xs text-slate-500">
                                    Select a team first.
                                </p>
                            )}

                        </div>

                        {/* CREDENTIAL */}

                        <div>

                            <label className="mb-2 block text-sm text-slate-300">
                                Credential
                            </label>

                            <select
                                value={
                                    selectedCredential
                                }
                                onChange={
                                    handleCredentialChange
                                }
                                disabled={
                                    credentialLoading
                                }
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500 disabled:cursor-not-allowed disabled:opacity-50"
                            >

                                <option value="">
                                    {credentialLoading
                                        ? "Loading credentials..."
                                        : "Select a credential"}
                                </option>

                                {credentials.map(
                                    (
                                        credential
                                    ) => (
                                        <option
                                            key={
                                                credential.id
                                            }
                                            value={
                                                credential.id
                                            }
                                        >
                                            {
                                                credential.title
                                            }
                                        </option>
                                    )
                                )}

                            </select>

                        </div>

                        {/* PERMISSION */}

                        <div>

                            <label className="mb-2 block text-sm text-slate-300">
                                Permission Level
                            </label>

                            <select
                                value={
                                    permissionLevel
                                }
                                onChange={(
                                    event
                                ) =>
                                    setPermissionLevel(
                                        event
                                            .target
                                            .value
                                    )
                                }
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                            >

                                <option value="VIEW_ONLY">
                                    VIEW ONLY
                                </option>

                                <option value="EDIT_ACCESS">
                                    EDIT ACCESS
                                </option>

                                <option value="FULL_MANAGEMENT">
                                    FULL MANAGEMENT
                                </option>

                            </select>

                            <p className="mt-2 text-xs text-slate-500">
                                {
                                    getPermissionDescription(
                                        permissionLevel
                                    )
                                }
                            </p>

                        </div>

                        {/* EXPIRATION */}

                        <div>

                            <label className="mb-2 block text-sm text-slate-300">
                                Expiration Date
                            </label>

                            <input
                                type="datetime-local"
                                step="60"
                                value={
                                    expirationDate
                                }
                                onChange={(
                                    event
                                ) =>
                                    setExpirationDate(
                                        event
                                            .target
                                            .value
                                    )
                                }
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                            />

                            <p className="mt-2 text-xs text-slate-500">
                                Leave empty for no expiration.
                            </p>

                        </div>

                        {/* SUBMIT */}

                        <div className="md:col-span-2">

                            <button
                                type="submit"
                                disabled={
                                    loading ||
                                    !selectedTeam ||
                                    !selectedMember ||
                                    !selectedCredential
                                }
                                className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-700 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                {loading
                                    ? "Granting Access..."
                                    : "Grant Credential Access"}
                            </button>

                        </div>

                    </form>

                </div>

                {/* =====================================================
                    CURRENT ACCESS
                ====================================================== */}

                {selectedTeam &&
                    selectedCredential && (

                        <div className="mt-8 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                            <div className="mb-6">

                                <h2 className="text-xl font-semibold">
                                    Current Access
                                </h2>

                                <p className="mt-2 text-sm text-slate-400">

                                    Team:{" "}

                                    <span className="text-white">
                                        {
                                            teams.find(
                                                (
                                                    team
                                                ) =>
                                                    String(
                                                        team.id
                                                    ) ===
                                                    String(
                                                        selectedTeam
                                                    )
                                            )?.name
                                        }
                                    </span>

                                    {" • "}

                                    Credential:{" "}

                                    <span className="text-white">
                                        {
                                            credentials.find(
                                                (
                                                    credential
                                                ) =>
                                                    String(
                                                        credential.id
                                                    ) ===
                                                    String(
                                                        selectedCredential
                                                    )
                                            )?.title
                                        }
                                    </span>

                                </p>

                            </div>

                            {accessLoading ? (

                                <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                                    Loading access information...
                                </div>

                            ) : accessList.length === 0 ? (

                                <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                                    No credential access records found.
                                </div>

                            ) : (

                                <div className="space-y-3">

                                    {accessList.map(
                                        (
                                            access
                                        ) => {

                                            const userId =
                                                access.userId ??
                                                access.sharedUserId;

                                            const username =
                                                access.username ??
                                                access.userUsername ??
                                                access.sharedUserUsername ??
                                                "Team Member";

                                            const email =
                                                access.email ??
                                                access.userEmail ??
                                                access.sharedUserEmail ??
                                                "";

                                            return (

                                                <div
                                                    key={
                                                        access.accessId ??
                                                        access.id ??
                                                        userId
                                                    }
                                                    className="flex flex-col gap-4 rounded-lg bg-slate-800 p-5 sm:flex-row sm:items-center sm:justify-between"
                                                >

                                                    <div>

                                                        <p className="font-semibold text-white">
                                                            {
                                                                username
                                                            }
                                                        </p>

                                                        {email && (
                                                            <p className="mt-1 text-sm text-slate-400">
                                                                {
                                                                    email
                                                                }
                                                            </p>
                                                        )}

                                                        {userId && (
                                                            <p className="mt-1 text-xs text-slate-500">
                                                                User ID:{" "}
                                                                {
                                                                    userId
                                                                }
                                                            </p>
                                                        )}

                                                        <p
                                                            className={`mt-2 text-xs ${
                                                                access.expirationDate
                                                                    ? "text-orange-400"
                                                                    : "text-green-400"
                                                            }`}
                                                        >
                                                            Expiration:{" "}
                                                            {formatExpirationDate(
                                                                access.expirationDate
                                                            )}
                                                        </p>

                                                    </div>

                                                    <div className="flex items-center gap-3">

                                                        <span
                                                            className={`rounded-full border px-3 py-1 text-xs font-medium ${getPermissionClass(
                                                                access.permissionLevel
                                                            )}`}
                                                        >
                                                            {
                                                                getPermissionLabel(
                                                                    access.permissionLevel
                                                                )
                                                            }
                                                        </span>

                                                        {userId && (
                                                            <button
                                                                type="button"
                                                                onClick={() =>
                                                                    removeAccess(
                                                                        userId
                                                                    )
                                                                }
                                                                className="rounded-lg bg-red-600 px-3 py-2 text-xs font-semibold hover:bg-red-700"
                                                            >
                                                                Remove
                                                            </button>
                                                        )}

                                                    </div>

                                                </div>

                                            );
                                        }
                                    )}

                                </div>

                            )}

                        </div>

                    )}

            </div>

        </div>
    );
}

export default TeamCredentialAccess;