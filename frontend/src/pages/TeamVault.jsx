import { useEffect, useState } from "react";

const API_BASE_URL = "http://localhost:8080";

function TeamVault() {

    // =========================================================
    // TEAMS
    // =========================================================

    const [teams, setTeams] = useState([]);
    const [selectedTeam, setSelectedTeam] = useState(null);

    const [teamName, setTeamName] = useState("");

    // =========================================================
    // MEMBERS
    // =========================================================

    const [members, setMembers] = useState([]);

    const [memberEmail, setMemberEmail] = useState("");
    const [memberRole, setMemberRole] = useState("MEMBER");

    // =========================================================
    // TEAM CREDENTIALS
    // =========================================================

    const [teamCredentials, setTeamCredentials] =
        useState([]);

    const [visiblePasswords, setVisiblePasswords] =
        useState({});

    // =========================================================
    // CREDENTIAL ACCESS
    // =========================================================

    const [selectedCredential, setSelectedCredential] =
        useState("");

    const [selectedMember, setSelectedMember] =
        useState("");

    const [permissionLevel, setPermissionLevel] =
        useState("VIEW_ONLY");

    const [expirationDate, setExpirationDate] =
        useState("");

    const [accessList, setAccessList] =
        useState([]);

    // =========================================================
    // PERSONAL CREDENTIALS
    // =========================================================

    const [credentials, setCredentials] =
        useState([]);

    // =========================================================
    // GENERAL STATE
    // =========================================================

    const [loading, setLoading] =
        useState(false);

    const [memberLoading, setMemberLoading] =
        useState(false);

    const [credentialLoading, setCredentialLoading] =
        useState(false);

    const [accessLoading, setAccessLoading] =
        useState(false);

    const [message, setMessage] =
        useState("");

    const [error, setError] =
        useState("");

    const token =
        localStorage.getItem("token");

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
        loadMyTeamCredentials();

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
                    data.message ||
                    "Failed to load teams."
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
    // LOAD PERSONAL CREDENTIALS
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
    // LOAD TEAM CREDENTIALS AVAILABLE TO CURRENT USER
    // =========================================================

    async function loadMyTeamCredentials() {

        try {

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

            setTeamCredentials(
                Array.isArray(data)
                    ? data
                    : []
            );

        } catch (err) {

            console.error(
                "Team credential loading error:",
                err
            );

        }
    }

    // =========================================================
    // LOAD MEMBERS
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
    // LOAD ACCESS LIST
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
    // SELECT TEAM
    // =========================================================

    async function handleSelectTeam(team) {

        setSelectedTeam(team);

        setMembers([]);

        setAccessList([]);

        setSelectedMember("");

        setSelectedCredential("");

        setMessage("");

        setError("");

        await loadMembers(team.id);
    }

    // =========================================================
    // CREATE TEAM
    // =========================================================

    async function createTeam(event) {

        event.preventDefault();

        setMessage("");
        setError("");

        if (!teamName.trim()) {

            setError(
                "Enter a team name."
            );

            return;
        }

        try {

            const response = await fetch(
                `${API_BASE_URL}/api/teams`,
                {
                    method: "POST",
                    headers: authHeaders,
                    body: JSON.stringify({
                        name: teamName.trim(),
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
                    "Failed to create team."
                );
            }

            setTeamName("");

            setMessage(
                "Team created successfully."
            );

            await loadTeams();

        } catch (err) {

            setError(err.message);

        }
    }

    // =========================================================
    // ADD MEMBER
    // =========================================================

    async function addMember(event) {

        event.preventDefault();

        setMessage("");
        setError("");

        if (!selectedTeam) {

            setError(
                "Select a team first."
            );

            return;
        }

        if (!memberEmail.trim()) {

            setError(
                "Enter the member email."
            );

            return;
        }

        try {

            const response = await fetch(
                `${API_BASE_URL}/api/teams/${selectedTeam.id}/members`,
                {
                    method: "POST",
                    headers: authHeaders,
                    body: JSON.stringify({
                        email: memberEmail.trim(),
                        role: memberRole,
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
                    "Failed to add member."
                );
            }

            setMemberEmail("");

            setMemberRole("MEMBER");

            setMessage(
                "Team member added successfully."
            );

            await loadMembers(
                selectedTeam.id
            );

        } catch (err) {

            setError(err.message);

        }
    }

    // =========================================================
    // REMOVE MEMBER
    // =========================================================

    async function removeMember(userId) {

        if (!selectedTeam) {
            return;
        }

        const confirmed =
            window.confirm(
                "Are you sure you want to remove this member?"
            );

        if (!confirmed) {
            return;
        }

        try {

            setMessage("");
            setError("");

            const response = await fetch(
                `${API_BASE_URL}/api/teams/${selectedTeam.id}/members/${userId}`,
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
                    "Failed to remove member."
                );
            }

            setMessage(
                "Team member removed successfully."
            );

            await loadMembers(
                selectedTeam.id
            );

        } catch (err) {

            setError(err.message);

        }
    }

    // =========================================================
    // SELECT CREDENTIAL
    // =========================================================

    async function handleCredentialChange(
        event
    ) {

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
                selectedTeam.id,
                credentialId
            );

        } else {

            setAccessList([]);

        }
    }

    // =========================================================
    // GRANT CREDENTIAL ACCESS
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

                userId:
                    Number(
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
                "Grant team credential access:",
                requestBody
            );

            const response = await fetch(
                `${API_BASE_URL}/api/team-access/teams/${selectedTeam.id}/credentials/${selectedCredential}`,
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
                    "Failed to grant credential access."
                );
            }

            setMessage(
                "Credential access granted successfully."
            );

            setExpirationDate("");

            await loadAccess(
                selectedTeam.id,
                selectedCredential
            );

            await loadMyTeamCredentials();

        } catch (err) {

            setError(err.message);

        } finally {

            setLoading(false);

        }
    }

    // =========================================================
    // REMOVE CREDENTIAL ACCESS
    // =========================================================

    async function removeCredentialAccess(
        userId
    ) {

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
                `${API_BASE_URL}/api/team-access/teams/${selectedTeam.id}/credentials/${selectedCredential}/users/${userId}`,
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
                    "Failed to remove credential access."
                );
            }

            setMessage(
                "Credential access removed successfully."
            );

            await loadAccess(
                selectedTeam.id,
                selectedCredential
            );

            await loadMyTeamCredentials();

        } catch (err) {

            setError(err.message);

        }
    }

    // =========================================================
    // SHOW / HIDE PASSWORD
    // =========================================================

    function togglePassword(id) {

        setVisiblePasswords(
            (previous) => ({
                ...previous,
                [id]:
                    !previous[id],
            })
        );
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
                return permission ||
                    "Unknown";
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
    // RENDER
    // =========================================================

    return (

        <div className="min-h-screen bg-slate-950 px-6 py-10 text-white">

            <div className="mx-auto max-w-7xl">

                {/* =====================================================
                    HEADER
                ====================================================== */}

                <div className="mb-8">

                    <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                        <div>

                            <h1 className="text-3xl font-bold">
                                Team Vault
                            </h1>

                            <p className="mt-2 text-slate-400">
                                Manage teams, members, credentials and access from one place.
                            </p>

                        </div>

                        <button
                            type="button"
                            onClick={async () => {

                                setMessage("");
                                setError("");

                                await loadTeams();

                                if (selectedTeam) {
                                    await loadMembers(
                                        selectedTeam.id
                                    );
                                }

                                await loadCredentials();
                                await loadMyTeamCredentials();

                                if (
                                    selectedTeam &&
                                    selectedCredential
                                ) {
                                    await loadAccess(
                                        selectedTeam.id,
                                        selectedCredential
                                    );
                                }

                            }}
                            className="rounded-lg bg-slate-700 px-5 py-3 font-semibold hover:bg-slate-600"
                        >
                            Refresh Everything
                        </button>

                    </div>

                </div>

                {/* =====================================================
                    ALERTS
                ====================================================== */}

                {message && (

                    <div className="mb-6 rounded-lg border border-green-500/30 bg-green-500/10 p-4 text-green-400">
                        ✓ {message}
                    </div>

                )}

                {error && (

                    <div className="mb-6 rounded-lg border border-red-500/30 bg-red-500/10 p-4 text-red-400">
                        ⚠ {error}
                    </div>

                )}

                {/* =====================================================
                    TOP SECTION
                ====================================================== */}

                <div className="grid gap-6 lg:grid-cols-2">

                    {/* =================================================
                        CREATE TEAM
                    ================================================== */}

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                        <h2 className="mb-5 text-xl font-semibold">
                            Create Team
                        </h2>

                        <form
                            onSubmit={createTeam}
                            className="space-y-4"
                        >

                            <div>

                                <label className="mb-2 block text-sm text-slate-300">
                                    Team Name
                                </label>

                                <input
                                    type="text"
                                    value={teamName}
                                    onChange={(event) =>
                                        setTeamName(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Enter team name"
                                    className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                />

                            </div>

                            <button
                                type="submit"
                                className="w-full rounded-lg bg-blue-600 px-4 py-3 font-semibold hover:bg-blue-700"
                            >
                                Create Team
                            </button>

                        </form>

                    </div>

                    {/* =================================================
                        SELECT TEAM
                    ================================================== */}

                    <div className="rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                        <h2 className="mb-5 text-xl font-semibold">
                            Select Team
                        </h2>

                        {loading && teams.length === 0 ? (

                            <p className="text-slate-400">
                                Loading teams...
                            </p>

                        ) : teams.length === 0 ? (

                            <p className="text-slate-400">
                                No teams created yet.
                            </p>

                        ) : (

                            <select
                                value={
                                    selectedTeam?.id || ""
                                }
                                onChange={(event) => {

                                    const team =
                                        teams.find(
                                            (item) =>
                                                String(
                                                    item.id
                                                ) ===
                                                event.target.value
                                        );

                                    if (team) {
                                        handleSelectTeam(
                                            team
                                        );
                                    }

                                }}
                                className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                            >

                                <option value="">
                                    Select a team
                                </option>

                                {teams.map(
                                    (team) => (

                                        <option
                                            key={team.id}
                                            value={team.id}
                                        >
                                            {team.name}
                                        </option>

                                    )
                                )}

                            </select>

                        )}

                    </div>

                </div>

                {/* =====================================================
                    TEAMS
                ====================================================== */}

                <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                    <div className="mb-5">

                        <h2 className="text-xl font-semibold">
                            Your Teams
                        </h2>

                        <p className="mt-1 text-sm text-slate-400">
                            Select a team to manage its members and credential access.
                        </p>

                    </div>

                    {teams.length === 0 ? (

                        <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                            No teams available.
                        </div>

                    ) : (

                        <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">

                            {teams.map(
                                (team) => (

                                    <button
                                        key={team.id}
                                        type="button"
                                        onClick={() =>
                                            handleSelectTeam(
                                                team
                                            )
                                        }
                                        className={`rounded-lg border p-5 text-left transition ${
                                            selectedTeam?.id ===
                                            team.id
                                                ? "border-blue-500 bg-blue-500/10"
                                                : "border-slate-700 bg-slate-800 hover:border-slate-500"
                                        }`}
                                    >

                                        <h3 className="text-lg font-semibold">
                                            {team.name}
                                        </h3>

                                        <p className="mt-2 text-sm text-slate-400">
                                            Team ID: {team.id}
                                        </p>

                                        <p className="mt-2 text-sm text-blue-400">
                                            {selectedTeam?.id ===
                                            team.id
                                                ? "✓ Selected"
                                                : "Click to manage"}
                                        </p>

                                    </button>

                                )
                            )}

                        </div>

                    )}

                </div>

                {/* =====================================================
                    SELECTED TEAM CONTENT
                ====================================================== */}

                {selectedTeam && (

                    <>

                        {/* =================================================
                            TEAM MEMBERS
                        ================================================== */}

                        <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                            <div className="mb-6">

                                <h2 className="text-xl font-semibold">
                                    Team Members
                                </h2>

                                <p className="mt-1 text-sm text-slate-400">
                                    Manage members of{" "}
                                    <span className="text-white">
                                        {selectedTeam.name}
                                    </span>
                                </p>

                            </div>

                            {/* ADD MEMBER */}

                            <form
                                onSubmit={addMember}
                                className="mb-6 grid gap-4 md:grid-cols-3"
                            >

                                <div>

                                    <label className="mb-2 block text-sm text-slate-300">
                                        Member Email
                                    </label>

                                    <input
                                        type="email"
                                        value={
                                            memberEmail
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            setMemberEmail(
                                                event.target
                                                    .value
                                            )
                                        }
                                        placeholder="user@example.com"
                                        className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                    />

                                </div>

                                <div>

                                    <label className="mb-2 block text-sm text-slate-300">
                                        Member Role
                                    </label>

                                    <select
                                        value={
                                            memberRole
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            setMemberRole(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                    >

                                        <option value="MEMBER">
                                            MEMBER
                                        </option>

                                        <option value="ADMIN">
                                            ADMIN
                                        </option>

                                    </select>

                                </div>

                                <div className="flex items-end">

                                    <button
                                        type="submit"
                                        className="w-full rounded-lg bg-emerald-600 px-4 py-3 font-semibold hover:bg-emerald-700"
                                    >
                                        Add Member
                                    </button>

                                </div>

                            </form>

                            {/* MEMBER LIST */}

                            {memberLoading ? (

                                <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                                    Loading members...
                                </div>

                            ) : members.length === 0 ? (

                                <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                                    No members found.
                                </div>

                            ) : (

                                <div className="space-y-3">

                                    {members.map(
                                        (member) => (

                                            <div
                                                key={
                                                    member.userId ??
                                                    member.id
                                                }
                                                className="flex flex-col gap-4 rounded-lg bg-slate-800 p-5 sm:flex-row sm:items-center sm:justify-between"
                                            >

                                                <div>

                                                    <p className="font-semibold">
                                                        {
                                                            member.username
                                                        }
                                                    </p>

                                                    <p className="mt-1 text-sm text-slate-400">
                                                        {
                                                            member.email
                                                        }
                                                    </p>

                                                    <p className="mt-1 text-xs text-slate-500">
                                                        User ID:{" "}
                                                        {
                                                            member.userId
                                                        }
                                                    </p>

                                                </div>

                                                <div className="flex items-center gap-3">

                                                    <span className="rounded-full bg-slate-700 px-3 py-1 text-xs font-medium">
                                                        {
                                                            member.role
                                                        }
                                                    </span>

                                                    {member.role !==
                                                        "OWNER" && (

                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                removeMember(
                                                                    member.userId
                                                                )
                                                            }
                                                            className="rounded-lg bg-red-600 px-3 py-2 text-xs font-semibold hover:bg-red-700"
                                                        >
                                                            Remove
                                                        </button>

                                                    )}

                                                </div>

                                            </div>

                                        )
                                    )}

                                </div>

                            )}

                        </div>

                        {/* =================================================
                            GRANT CREDENTIAL ACCESS
                        ================================================== */}

                        <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                            <div className="mb-6">

                                <h2 className="text-xl font-semibold">
                                    Team Credential Access
                                </h2>

                                <p className="mt-1 text-sm text-slate-400">
                                    Give team members controlled access to your credentials.
                                </p>

                            </div>

                            <form
                                onSubmit={grantAccess}
                                className="grid gap-5 md:grid-cols-2"
                            >

                                {/* MEMBER */}

                                <div>

                                    <label className="mb-2 block text-sm text-slate-300">
                                        Team Member
                                    </label>

                                    <select
                                        value={
                                            selectedMember
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            setSelectedMember(
                                                event.target
                                                    .value
                                            )
                                        }
                                        disabled={
                                            memberLoading
                                        }
                                        className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500 disabled:opacity-50"
                                    >

                                        <option value="">
                                            Select team member
                                        </option>

                                        {members
                                            .filter(
                                                (
                                                    member
                                                ) =>
                                                    member.role !==
                                                    "OWNER"
                                            )
                                            .map(
                                                (
                                                    member
                                                ) => (

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
                                        className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500 disabled:opacity-50"
                                    >

                                        <option value="">
                                            Select credential
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
                                                event.target
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
                                        Expiration
                                    </label>

                                    <input
                                        type="datetime-local"
                                        value={
                                            expirationDate
                                        }
                                        onChange={(
                                            event
                                        ) =>
                                            setExpirationDate(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full rounded-lg border border-slate-600 bg-slate-800 px-4 py-3 text-white outline-none focus:border-blue-500"
                                    />

                                    <p className="mt-2 text-xs text-slate-500">
                                        Leave empty for permanent access.
                                    </p>

                                </div>

                                {/* GRANT */}

                                <div className="md:col-span-2">

                                    <button
                                        type="submit"
                                        disabled={
                                            loading ||
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

                        {/* =================================================
                            CURRENT ACCESS
                        ================================================== */}

                        {selectedCredential && (

                            <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                                <div className="mb-6">

                                    <h2 className="text-xl font-semibold">
                                        Current Credential Access
                                    </h2>

                                    <p className="mt-1 text-sm text-slate-400">

                                        Showing access for{" "}

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
                                        Loading access...
                                    </div>

                                ) : accessList.length === 0 ? (

                                    <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                                        No users currently have access to this credential.
                                    </div>

                                ) : (

                                    <div className="space-y-3">

                                        {accessList.map(
                                            (access) => {

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

                                                            <p className="font-semibold">
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

                                                        </div>

                                                        <div className="flex items-center gap-3">

                                                            <span
                                                                className={`rounded-full border px-3 py-1 text-xs font-semibold ${getPermissionClass(
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
                                                                        removeCredentialAccess(
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

                    </>

                )}

                {/* =====================================================
                    TEAM CREDENTIALS AVAILABLE TO CURRENT USER
                ====================================================== */}

                <div className="mt-6 rounded-xl border border-slate-700 bg-slate-900 p-6 shadow-lg">

                    <div className="mb-6">

                        <h2 className="text-xl font-semibold">
                            Team Credentials
                        </h2>

                        <p className="mt-1 text-sm text-slate-400">
                            Credentials you can access through team permissions.
                        </p>

                    </div>

                    {teamCredentials.length === 0 ? (

                        <div className="rounded-lg bg-slate-800 p-5 text-slate-400">
                            No team credentials available.
                        </div>

                    ) : (

                        <div className="grid gap-5 md:grid-cols-2">

                            {teamCredentials.map(
                                (credential) => {

                                    const credentialId =
                                        credential.credentialId ??
                                        credential.id;

                                    const passwordVisible =
                                        visiblePasswords[
                                            credentialId
                                        ];

                                    return (

                                        <div
                                            key={
                                                credentialId
                                            }
                                            className="rounded-xl border border-slate-700 bg-slate-800 p-5"
                                        >

                                            <div className="flex items-start justify-between gap-3">

                                                <div>

                                                    <h3 className="font-semibold text-lg">
                                                        {
                                                            credential.title
                                                        }
                                                    </h3>

                                                    <p className="mt-1 text-xs text-slate-500">
                                                        Team Credential
                                                    </p>

                                                </div>

                                                <span
                                                    className={`rounded-full border px-3 py-1 text-xs font-semibold ${getPermissionClass(
                                                        credential.permissionLevel
                                                    )}`}
                                                >
                                                    {
                                                        getPermissionLabel(
                                                            credential.permissionLevel
                                                        )
                                                    }
                                                </span>

                                            </div>

                                            <div className="mt-4">

                                                <p className="text-xs text-slate-500">
                                                    Username
                                                </p>

                                                <div className="mt-1 rounded-lg bg-slate-900 px-4 py-3 text-sm">
                                                    {
                                                        credential.username
                                                    }
                                                </div>

                                            </div>

                                            <div className="mt-4">

                                                <p className="text-xs text-slate-500">
                                                    Password
                                                </p>

                                                <div className="mt-1 flex gap-2">

                                                    <div className="flex-1 overflow-hidden rounded-lg bg-slate-900 px-4 py-3 font-mono text-sm">

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
                                                        className="rounded-lg bg-slate-700 px-4 py-2 text-sm font-semibold hover:bg-slate-600"
                                                    >
                                                        {passwordVisible
                                                            ? "Hide"
                                                            : "Show"}
                                                    </button>

                                                </div>

                                            </div>

                                            <p className="mt-4 text-xs text-slate-500">
                                                {
                                                    getPermissionDescription(
                                                        credential.permissionLevel
                                                    )
                                                }
                                            </p>

                                        </div>

                                    );
                                }
                            )}

                        </div>

                    )}

                </div>

            </div>

        </div>
    );
}

export default TeamVault;