import { useEffect, useState } from "react";

import { generatePassword } from "../utils/passwordGenerator";
import { analyzePasswordStrength } from "../utils/passwordStrength";

function Vault() {
    // =========================================================
    // MY CREDENTIALS
    // =========================================================

    const [credentials, setCredentials] = useState([]);

    // =========================================================
    // SHARED CREDENTIALS
    // =========================================================

    const [sharedCredentials, setSharedCredentials] = useState([]);
    const [sharedLoading, setSharedLoading] = useState(true);

    // =========================================================
    // CREDENTIAL FORM
    // =========================================================

    const [title, setTitle] = useState("");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");

    const [editingId, setEditingId] = useState(null);

    // =========================================================
    // SHARING FORM
    // =========================================================

    const [sharingCredentialId, setSharingCredentialId] =
        useState(null);

    const [sharingEmail, setSharingEmail] =
        useState("");

    const [sharingPermission, setSharingPermission] =
        useState("VIEW_ONLY");

    const [sharingExpiration, setSharingExpiration] =
        useState("");

    // =========================================================
    // SHARED CREDENTIAL EDIT
    // =========================================================

    const [editingSharedId, setEditingSharedId] =
        useState(null);

    const [sharedTitle, setSharedTitle] =
        useState("");

    const [sharedUsername, setSharedUsername] =
        useState("");

    const [sharedPassword, setSharedPassword] =
        useState("");

    // =========================================================
    // MANAGE SHARED ACCESS
    // =========================================================

    const [managingCredentialId, setManagingCredentialId] =
        useState(null);

    const [managedShares, setManagedShares] =
        useState([]);

    const [manageLoading, setManageLoading] =
        useState(false);

    const [manageEmail, setManageEmail] =
        useState("");

    const [managePermission, setManagePermission] =
        useState("VIEW_ONLY");

    const [manageExpiration, setManageExpiration] =
        useState("");

    // =========================================================
    // GENERAL STATE
    // =========================================================

    const [message, setMessage] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    // =========================================================
    // PASSWORD GENERATOR
    // =========================================================

    const [passwordLength, setPasswordLength] =
        useState(16);

    const [useUppercase, setUseUppercase] =
        useState(true);

    const [useLowercase, setUseLowercase] =
        useState(true);

    const [useNumbers, setUseNumbers] =
        useState(true);

    const [useSpecial, setUseSpecial] =
        useState(true);

    const token = localStorage.getItem("token");

    // =========================================================
    // FETCH MY CREDENTIALS
    // =========================================================

    const fetchCredentials = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/vault/credentials",
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Failed to load credentials"
                );
            }

            setCredentials(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    // =========================================================
    // FETCH SHARED WITH ME
    // =========================================================

    const fetchSharedCredentials = async () => {
        try {
            setSharedLoading(true);

            const response = await fetch(
                "http://localhost:8080/api/sharing/shared-with-me",
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Failed to load shared credentials"
                );
            }

            setSharedCredentials(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setSharedLoading(false);
        }
    };

    // =========================================================
    // INITIAL LOAD
    // =========================================================

    useEffect(() => {
        if (!token) {
            setError("You are not logged in.");
            setLoading(false);
            setSharedLoading(false);
            return;
        }

        fetchCredentials();
        fetchSharedCredentials();
    }, []);

    // =========================================================
    // PASSWORD ANALYSIS
    // =========================================================

    const passwordAnalysis =
        analyzePasswordStrength(password);

    // =========================================================
    // GENERATE PASSWORD
    // =========================================================

    const handleGeneratePassword = () => {
        setMessage("");
        setError("");

        try {
            const selectedCharacterTypes = [
                useUppercase,
                useLowercase,
                useNumbers,
                useSpecial,
            ].filter(Boolean).length;

            if (selectedCharacterTypes === 0) {
                setError(
                    "Select at least one character type."
                );
                return;
            }

            if (
                Number(passwordLength) <
                selectedCharacterTypes
            ) {
                setError(
                    `Password length must be at least ${selectedCharacterTypes}.`
                );
                return;
            }

            const generatedPassword =
                generatePassword({
                    length: Number(passwordLength),
                    useUppercase,
                    useLowercase,
                    useNumbers,
                    useSpecial,
                });

            setPassword(generatedPassword);

            setMessage(
                "Secure password generated successfully."
            );
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // EDIT MY CREDENTIAL
    // =========================================================

    const handleEdit = (credential) => {
        setEditingId(credential.id);

        setTitle(credential.title);
        setUsername(credential.username);
        setPassword(credential.password);

        setMessage("");
        setError("");
    };

    // =========================================================
    // CANCEL MY CREDENTIAL EDIT
    // =========================================================

    const handleCancelEdit = () => {
        setEditingId(null);

        setTitle("");
        setUsername("");
        setPassword("");

        setMessage("");
        setError("");
    };

    // =========================================================
    // SAVE / UPDATE MY CREDENTIAL
    // =========================================================

    const handleSubmit = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (
            !title.trim() ||
            !username.trim() ||
            !password.trim()
        ) {
            setError(
                "Title, username and password are required."
            );
            return;
        }

        try {
            const isEditing = editingId !== null;

            const url = isEditing
                ? `http://localhost:8080/api/vault/credentials/${editingId}`
                : "http://localhost:8080/api/vault/credentials";

            const method = isEditing
                ? "PUT"
                : "POST";

            const response = await fetch(url, {
                method,
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                    title: title.trim(),
                    username: username.trim(),
                    password,
                }),
            });

            let data = {};

            try {
                data = await response.json();
            } catch {
                // No response body
            }

            if (!response.ok) {
                throw new Error(
                    data.message ||
                        "Credential operation failed"
                );
            }

            setMessage(
                isEditing
                    ? "Credential updated successfully."
                    : "Credential added successfully."
            );

            setEditingId(null);

            setTitle("");
            setUsername("");
            setPassword("");

            fetchCredentials();
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // DELETE MY CREDENTIAL
    // =========================================================

    const handleDelete = async (id) => {
        const confirmed = window.confirm(
            "Are you sure you want to delete this credential?"
        );

        if (!confirmed) {
            return;
        }

        setMessage("");
        setError("");

        try {
            const response = await fetch(
                `http://localhost:8080/api/vault/credentials/${id}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Failed to delete credential"
                );
            }

            setMessage(
                "Credential deleted successfully."
            );

            if (editingId === id) {
                handleCancelEdit();
            }

            fetchCredentials();
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // OPEN SHARE FORM
    // =========================================================

    const handleOpenShare = (credentialId) => {
        setSharingCredentialId(credentialId);

        setSharingEmail("");
        setSharingPermission("VIEW_ONLY");
        setSharingExpiration("");

        setMessage("");
        setError("");
    };

    // =========================================================
    // CLOSE SHARE FORM
    // =========================================================

    const handleCloseShare = () => {
        setSharingCredentialId(null);

        setSharingEmail("");
        setSharingPermission("VIEW_ONLY");
        setSharingExpiration("");

        setMessage("");
        setError("");
    };

    // =========================================================
    // SHARE CREDENTIAL
    // =========================================================

    const handleShareCredential = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (!sharingEmail.trim()) {
            setError(
                "Enter the email address of the user."
            );
            return;
        }

        try {
            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${sharingCredentialId}`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                        sharedUserEmail:
                            sharingEmail.trim(),

                        permissionLevel:
                            sharingPermission,

                        expirationDate:
                            sharingExpiration
                                ? sharingExpiration
                                : null,
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
                        "Failed to share credential"
                );
            }

            setMessage(
                "Credential shared successfully."
            );

            handleCloseShare();
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // VIEW SHARED CREDENTIAL
    // =========================================================

    const handleViewSharedCredential = async (
        sharedCredential
    ) => {
        setMessage("");
        setError("");

        try {
            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${sharedCredential.credentialId}`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Unable to access shared credential"
                );
            }

            window.alert(
                `Service: ${data.title}\n\n` +
                `Username: ${data.username}\n\n` +
                `Password: ${data.password}`
            );
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // OPEN EDIT FOR SHARED CREDENTIAL
    // =========================================================

    const handleEditSharedCredential = (
        sharedCredential
    ) => {
        const permission =
            sharedCredential.permissionLevel;

        if (permission === "VIEW_ONLY") {
            setError(
                "You have View Only permission and cannot edit this credential."
            );
            return;
        }

        setEditingSharedId(
            sharedCredential.credentialId
        );

        setSharedTitle(
            sharedCredential.title || ""
        );

        setSharedUsername(
            sharedCredential.username || ""
        );

        setSharedPassword(
            sharedCredential.password || ""
        );

        setMessage("");
        setError("");
    };

    // =========================================================
    // CANCEL SHARED CREDENTIAL EDIT
    // =========================================================

    const handleCancelSharedEdit = () => {
        setEditingSharedId(null);

        setSharedTitle("");
        setSharedUsername("");
        setSharedPassword("");

        setMessage("");
        setError("");
    };

    // =========================================================
    // UPDATE SHARED CREDENTIAL
    // =========================================================

    const handleUpdateSharedCredential = async (
        event
    ) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (
            !sharedTitle.trim() ||
            !sharedUsername.trim() ||
            !sharedPassword.trim()
        ) {
            setError(
                "Title, username and password are required."
            );
            return;
        }

        try {
            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${editingSharedId}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                        title: sharedTitle.trim(),
                        username:
                            sharedUsername.trim(),
                        password: sharedPassword,
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
                        "Failed to update shared credential"
                );
            }

            setMessage(
                "Shared credential updated successfully."
            );

            handleCancelSharedEdit();

            fetchSharedCredentials();
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // OPEN MANAGE ACCESS
    // =========================================================

    const handleOpenManageAccess = async (
        sharedCredential
    ) => {
        if (
            sharedCredential.permissionLevel !==
            "FULL_MANAGEMENT"
        ) {
            setError(
                "Full Management permission is required to manage access."
            );
            return;
        }

        setManagingCredentialId(
            sharedCredential.credentialId
        );
        setManageEmail("");
        setManagePermission("VIEW_ONLY");
        setManageExpiration("");
        setMessage("");
        setError("");

        try {
            setManageLoading(true);

            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${sharedCredential.credentialId}/shares`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Failed to load shared access"
                );
            }

            setManagedShares(data);
        } catch (err) {
            setError(err.message);
            setManagingCredentialId(null);
        } finally {
            setManageLoading(false);
        }
    };

    // =========================================================
    // CLOSE MANAGE ACCESS
    // =========================================================

    const handleCloseManageAccess = () => {
        setManagingCredentialId(null);
        setManagedShares([]);
        setManageEmail("");
        setManagePermission("VIEW_ONLY");
        setManageExpiration("");
        setMessage("");
        setError("");
    };

    // =========================================================
    // ADD SHARED ACCESS AS MANAGER
    // =========================================================

    const handleManagerShare = async (event) => {
        event.preventDefault();

        setMessage("");
        setError("");

        if (!manageEmail.trim()) {
            setError(
                "Enter the email address of the user."
            );
            return;
        }

        try {
            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${managingCredentialId}/manage`,
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`,
                    },
                    body: JSON.stringify({
                        sharedUserEmail:
                            manageEmail.trim(),
                        permissionLevel:
                            managePermission,
                        expirationDate:
                            manageExpiration
                                ? manageExpiration
                                : null,
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
                        "Failed to share credential"
                );
            }

            setMessage(
                "Credential access granted successfully."
            );

            setManageEmail("");
            setManagePermission("VIEW_ONLY");
            setManageExpiration("");

            const refreshResponse = await fetch(
                `http://localhost:8080/api/sharing/credentials/${managingCredentialId}/shares`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (refreshResponse.ok) {
                const refreshedData =
                    await refreshResponse.json();
                setManagedShares(refreshedData);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // REMOVE SHARED ACCESS AS MANAGER
    // =========================================================

    const handleManagerRemoveAccess = async (
        sharedUserId
    ) => {
        const confirmed = window.confirm(
            "Are you sure you want to remove this user's access?"
        );

        if (!confirmed) {
            return;
        }

        setMessage("");
        setError("");

        try {
            const response = await fetch(
                `http://localhost:8080/api/sharing/credentials/${managingCredentialId}/manage/users/${sharedUserId}`,
                {
                    method: "DELETE",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
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
                        "Failed to remove shared access"
                );
            }

            setMessage(
                "Shared access removed successfully."
            );

            const refreshResponse = await fetch(
                `http://localhost:8080/api/sharing/credentials/${managingCredentialId}/shares`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            if (refreshResponse.ok) {
                const refreshedData =
                    await refreshResponse.json();
                setManagedShares(refreshedData);
            }
        } catch (err) {
            setError(err.message);
        }
    };

    // =========================================================
    // PERMISSION LABEL
    // =========================================================

    const getPermissionLabel = (
        permission
    ) => {
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
    };

    // =========================================================
    // PERMISSION DESCRIPTION
    // =========================================================

    const getPermissionDescription = (
        permission
    ) => {
        switch (permission) {
            case "VIEW_ONLY":
                return "Can view the credential but cannot modify it.";

            case "EDIT_ACCESS":
                return "Can view and update the credential.";

            case "FULL_MANAGEMENT":
                return "Can manage the credential according to project permissions.";

            default:
                return "";
        }
    };

    // =========================================================
    // PERMISSION CSS
    // =========================================================

    const getPermissionClass = (
        permission
    ) => {
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
    };

    // =========================================================
    // STRENGTH WIDTH
    // =========================================================

    const getStrengthWidth = () => {
        if (!password) {
            return "0%";
        }

        if (passwordAnalysis.score <= 2) {
            return "25%";
        }

        if (passwordAnalysis.score <= 4) {
            return "50%";
        }

        if (passwordAnalysis.score <= 6) {
            return "75%";
        }

        return "100%";
    };

    // =========================================================
    // STRENGTH COLOR
    // =========================================================

    const getStrengthColor = () => {
        if (!password) {
            return "text-slate-500";
        }

        if (passwordAnalysis.score <= 2) {
            return "text-red-400";
        }

        if (passwordAnalysis.score <= 4) {
            return "text-yellow-400";
        }

        if (passwordAnalysis.score <= 6) {
            return "text-emerald-400";
        }

        return "text-cyan-400";
    };

    // =========================================================
    // RENDER
    // =========================================================

    return (
        <div className="min-h-screen bg-slate-950 text-white relative overflow-hidden">

            {/* =====================================================
                BACKGROUND
            ====================================================== */}

            <div className="fixed inset-0 pointer-events-none">

                <div className="absolute -top-48 -left-48 w-[500px] h-[500px] bg-cyan-500/10 rounded-full blur-3xl animate-pulse"></div>

                <div className="absolute top-1/3 -right-48 w-[500px] h-[500px] bg-violet-600/10 rounded-full blur-3xl animate-pulse"></div>

                <div className="absolute -bottom-48 left-1/3 w-[500px] h-[500px] bg-blue-600/10 rounded-full blur-3xl"></div>

            </div>

            {/* =====================================================
                MAIN CONTAINER
            ====================================================== */}

            <div className="relative z-10 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">

                {/* =================================================
                    HEADER
                ================================================== */}

                <header className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-5 mb-8">

                    <div className="flex items-center gap-4">

                        <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-cyan-400 to-blue-600 flex items-center justify-center shadow-lg shadow-cyan-500/20">
                            <span className="text-2xl">
                                🔐
                            </span>
                        </div>

                        <div>

                            <h1 className="text-2xl sm:text-3xl font-bold bg-gradient-to-r from-cyan-400 to-blue-500 bg-clip-text text-transparent">
                                SecureVault
                            </h1>

                            <p className="text-slate-500 text-sm mt-1">
                                Your secure password management system
                            </p>

                        </div>

                    </div>

                    <div className="flex items-center gap-2 px-4 py-2 rounded-full bg-emerald-500/10 border border-emerald-500/20">

                        <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse"></span>

                        <span className="text-xs text-emerald-400 font-medium">
                            Vault Protected
                        </span>

                    </div>

                </header>

                {/* =================================================
                    ALERTS
                ================================================== */}

                {message && (
                    <div className="mb-6 rounded-2xl border border-emerald-500/20 bg-emerald-500/10 px-5 py-4 text-sm text-emerald-400">
                        ✓ {message}
                    </div>
                )}

                {error && (
                    <div className="mb-6 rounded-2xl border border-red-500/20 bg-red-500/10 px-5 py-4 text-sm text-red-400">
                        ⚠ {error}
                    </div>
                )}

                {/* =================================================
                    MAIN GRID
                ================================================== */}

                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

                    {/* =================================================
                        LEFT - ADD / UPDATE
                    ================================================== */}

                    <div className="bg-slate-900/70 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-2xl">

                        <div className="mb-6">

                            <div className="flex items-center gap-3">

                                <div className="w-10 h-10 rounded-xl bg-cyan-500/10 flex items-center justify-center">
                                    🔑
                                </div>

                                <div>

                                    <h2 className="text-xl font-bold">
                                        {editingId !== null
                                            ? "Update Credential"
                                            : "Add Credential"}
                                    </h2>

                                    <p className="text-slate-500 text-xs mt-1">
                                        Store your credentials securely
                                    </p>

                                </div>

                            </div>

                        </div>

                        <form
                            onSubmit={handleSubmit}
                            className="space-y-5"
                        >

                            {/* TITLE */}

                            <div>

                                <label
                                    htmlFor="title"
                                    className="block text-sm font-medium text-slate-300 mb-2"
                                >
                                    Service / Title
                                </label>

                                <input
                                    id="title"
                                    type="text"
                                    value={title}
                                    onChange={(event) =>
                                        setTitle(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Example: GitHub"
                                    className="w-full h-12 px-4 rounded-xl bg-slate-950/80 border border-slate-700 text-white placeholder-slate-600 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                />

                            </div>

                            {/* USERNAME */}

                            <div>

                                <label
                                    htmlFor="username"
                                    className="block text-sm font-medium text-slate-300 mb-2"
                                >
                                    Username
                                </label>

                                <input
                                    id="username"
                                    type="text"
                                    value={username}
                                    onChange={(event) =>
                                        setUsername(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Enter username"
                                    className="w-full h-12 px-4 rounded-xl bg-slate-950/80 border border-slate-700 text-white placeholder-slate-600 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                />

                            </div>

                            {/* PASSWORD */}

                            <div>

                                <label
                                    htmlFor="password"
                                    className="block text-sm font-medium text-slate-300 mb-2"
                                >
                                    Password
                                </label>

                                <input
                                    id="password"
                                    type="text"
                                    value={password}
                                    onChange={(event) =>
                                        setPassword(
                                            event.target.value
                                        )
                                    }
                                    placeholder="Enter or generate a password"
                                    className="w-full h-12 px-4 rounded-xl bg-slate-950/80 border border-slate-700 text-white placeholder-slate-600 outline-none transition focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                />

                            </div>

                            {/* PASSWORD STRENGTH */}

                            <div className="rounded-2xl bg-slate-950/60 border border-slate-800 p-4">

                                <div className="flex items-center justify-between mb-2">

                                    <span className="text-xs text-slate-500">
                                        Password Strength
                                    </span>

                                    <span
                                        className={`text-sm font-bold ${getStrengthColor()}`}
                                    >
                                        {passwordAnalysis.strength}
                                    </span>

                                </div>

                                <div className="h-2 bg-slate-800 rounded-full overflow-hidden">

                                    <div
                                        className="h-full rounded-full bg-gradient-to-r from-red-500 via-yellow-400 via-emerald-400 to-cyan-400 transition-all duration-500"
                                        style={{
                                            width: getStrengthWidth(),
                                        }}
                                    ></div>

                                </div>

                                {password &&
                                    passwordAnalysis
                                        .suggestions
                                        .length > 0 && (

                                        <div className="mt-4 pt-4 border-t border-slate-800">

                                            <p className="text-xs font-semibold text-slate-300 mb-2">
                                                Improve your password
                                            </p>

                                            <ul className="space-y-1">

                                                {passwordAnalysis.suggestions.map(
                                                    (
                                                        suggestion,
                                                        index
                                                    ) => (

                                                        <li
                                                            key={
                                                                index
                                                            }
                                                            className="text-xs text-slate-500 flex gap-2"
                                                        >

                                                            <span className="text-yellow-400">
                                                                •
                                                            </span>

                                                            {suggestion}

                                                        </li>

                                                    )
                                                )}

                                            </ul>

                                        </div>

                                    )}

                            </div>

                            {/* PASSWORD GENERATOR */}

                            <div className="rounded-2xl bg-gradient-to-br from-cyan-500/5 to-violet-500/5 border border-cyan-500/10 p-5">

                                <div className="flex items-center gap-3 mb-5">

                                    <div className="w-9 h-9 rounded-lg bg-cyan-500/10 flex items-center justify-center">
                                        ⚡
                                    </div>

                                    <div>

                                        <h3 className="font-semibold text-sm">
                                            Password Generator
                                        </h3>

                                        <p className="text-xs text-slate-500">
                                            Create a strong password automatically
                                        </p>

                                    </div>

                                </div>

                                {/* LENGTH */}

                                <div className="mb-4">

                                    <div className="flex items-center justify-between mb-2">

                                        <label className="text-xs text-slate-400">
                                            Password Length
                                        </label>

                                        <span className="text-xs font-bold text-cyan-400">
                                            {passwordLength}
                                        </span>

                                    </div>

                                    <input
                                        type="range"
                                        min="4"
                                        max="64"
                                        value={passwordLength}
                                        onChange={(event) =>
                                            setPasswordLength(
                                                Number(
                                                    event.target
                                                        .value
                                                )
                                            )
                                        }
                                        className="w-full accent-cyan-500 cursor-pointer"
                                    />

                                </div>

                                {/* CHARACTER OPTIONS */}

                                <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">

                                    <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/50 border border-slate-800 hover:border-cyan-500/30 cursor-pointer transition">

                                        <input
                                            type="checkbox"
                                            checked={
                                                useUppercase
                                            }
                                            onChange={(
                                                event
                                            ) =>
                                                setUseUppercase(
                                                    event.target
                                                        .checked
                                                )
                                            }
                                            className="w-4 h-4 accent-cyan-500"
                                        />

                                        <span className="text-xs text-slate-400">
                                            Uppercase A-Z
                                        </span>

                                    </label>

                                    <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/50 border border-slate-800 hover:border-cyan-500/30 cursor-pointer transition">

                                        <input
                                            type="checkbox"
                                            checked={
                                                useLowercase
                                            }
                                            onChange={(
                                                event
                                            ) =>
                                                setUseLowercase(
                                                    event.target
                                                        .checked
                                                )
                                            }
                                            className="w-4 h-4 accent-cyan-500"
                                        />

                                        <span className="text-xs text-slate-400">
                                            Lowercase a-z
                                        </span>

                                    </label>

                                    <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/50 border border-slate-800 hover:border-cyan-500/30 cursor-pointer transition">

                                        <input
                                            type="checkbox"
                                            checked={
                                                useNumbers
                                            }
                                            onChange={(
                                                event
                                            ) =>
                                                setUseNumbers(
                                                    event.target
                                                        .checked
                                                )
                                            }
                                            className="w-4 h-4 accent-cyan-500"
                                        />

                                        <span className="text-xs text-slate-400">
                                            Numbers 0-9
                                        </span>

                                    </label>

                                    <label className="flex items-center gap-3 p-3 rounded-xl bg-slate-950/50 border border-slate-800 hover:border-cyan-500/30 cursor-pointer transition">

                                        <input
                                            type="checkbox"
                                            checked={
                                                useSpecial
                                            }
                                            onChange={(
                                                event
                                            ) =>
                                                setUseSpecial(
                                                    event.target
                                                        .checked
                                                )
                                            }
                                            className="w-4 h-4 accent-cyan-500"
                                        />

                                        <span className="text-xs text-slate-400">
                                            Special characters
                                        </span>

                                    </label>

                                </div>

                                <button
                                    type="button"
                                    onClick={
                                        handleGeneratePassword
                                    }
                                    className="w-full mt-4 h-11 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 font-semibold text-sm shadow-lg shadow-cyan-500/10 hover:shadow-cyan-500/25 hover:-translate-y-0.5 transition"
                                >
                                    ⚡ Generate Secure Password
                                </button>

                            </div>

                            {/* FORM BUTTONS */}

                            <div className="flex gap-3 pt-1">

                                <button
                                    type="submit"
                                    className="flex-1 h-12 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 font-semibold text-sm shadow-lg shadow-cyan-500/15 hover:shadow-cyan-500/30 hover:-translate-y-0.5 transition"
                                >
                                    {editingId !== null
                                        ? "Update Credential"
                                        : "Save Credential"}
                                </button>

                                {editingId !== null && (

                                    <button
                                        type="button"
                                        onClick={
                                            handleCancelEdit
                                        }
                                        className="px-5 h-12 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 font-semibold text-sm hover:bg-slate-700 transition"
                                    >
                                        Cancel
                                    </button>

                                )}

                            </div>

                        </form>

                    </div>

                    {/* =================================================
                        RIGHT - SAVED CREDENTIALS
                    ================================================== */}

                    <div className="bg-slate-900/70 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-2xl">

                        <div className="flex items-center justify-between mb-6">

                            <div className="flex items-center gap-3">

                                <div className="w-10 h-10 rounded-xl bg-violet-500/10 flex items-center justify-center">
                                    🗄️
                                </div>

                                <div>

                                    <h2 className="text-xl font-bold">
                                        Saved Credentials
                                    </h2>

                                    <p className="text-slate-500 text-xs mt-1">
                                        {credentials.length}{" "}
                                        {credentials.length ===
                                        1
                                            ? "credential"
                                            : "credentials"}{" "}
                                        stored
                                    </p>

                                </div>

                            </div>

                            <div className="px-3 py-1.5 rounded-full bg-slate-950 border border-slate-800 text-xs text-slate-400">
                                {credentials.length}
                            </div>

                        </div>

                        {loading ? (

                            <div className="flex flex-col items-center justify-center py-20">

                                <div className="w-10 h-10 border-2 border-slate-700 border-t-cyan-400 rounded-full animate-spin"></div>

                                <p className="text-sm text-slate-500 mt-4">
                                    Loading credentials...
                                </p>

                            </div>

                        ) : credentials.length === 0 ? (

                            <div className="text-center py-16 px-5 rounded-2xl border border-dashed border-slate-800 bg-slate-950/30">

                                <div className="text-4xl mb-4">
                                    🔒
                                </div>

                                <h3 className="font-semibold text-slate-300">
                                    Your vault is empty
                                </h3>

                                <p className="text-xs text-slate-600 mt-2">
                                    Add your first credential using the form.
                                </p>

                            </div>

                        ) : (

                            <div className="space-y-3">

                                {credentials.map(
                                    (credential) => (

                                        <div
                                            key={
                                                credential.id
                                            }
                                            className="group rounded-2xl bg-slate-950/50 border border-slate-800 p-5 hover:border-cyan-500/20 hover:bg-slate-950/80 transition"
                                        >

                                            {/* CARD HEADER */}

                                            <div className="flex items-center justify-between gap-4 mb-4">

                                                <div className="flex items-center gap-3 min-w-0">

                                                    <div className="w-10 h-10 shrink-0 rounded-xl bg-gradient-to-br from-slate-800 to-slate-900 border border-slate-700 flex items-center justify-center">
                                                        🔑
                                                    </div>

                                                    <div className="min-w-0">

                                                        <h3 className="font-semibold text-slate-200 truncate">
                                                            {
                                                                credential.title
                                                            }
                                                        </h3>

                                                        <p className="text-xs text-slate-600 mt-1">
                                                            Credential
                                                        </p>

                                                    </div>

                                                </div>

                                            </div>

                                            {/* CREDENTIAL DATA */}

                                            <div className="space-y-2">

                                                <div className="flex items-center justify-between gap-4 rounded-lg bg-slate-900/70 px-3 py-2">

                                                    <span className="text-xs text-slate-600">
                                                        Username
                                                    </span>

                                                    <span className="text-xs text-slate-400 truncate">
                                                        {
                                                            credential.username
                                                        }
                                                    </span>

                                                </div>

                                                <div className="flex items-center justify-between gap-4 rounded-lg bg-slate-900/70 px-3 py-2">

                                                    <span className="text-xs text-slate-600">
                                                        Password
                                                    </span>

                                                    <span className="text-xs text-slate-400 font-mono truncate max-w-[65%]">
                                                        {
                                                            credential.password
                                                        }
                                                    </span>

                                                </div>

                                            </div>

                                            {/* ACTION BUTTONS */}

                                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 mt-4">

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        handleEdit(
                                                            credential
                                                        )
                                                    }
                                                    className="h-9 rounded-lg bg-slate-800 border border-slate-700 text-xs font-semibold text-slate-300 hover:bg-slate-700 hover:text-white transition"
                                                >
                                                    ✏️ Edit
                                                </button>

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        handleDelete(
                                                            credential.id
                                                        )
                                                    }
                                                    className="h-9 rounded-lg bg-red-500/5 border border-red-500/10 text-xs font-semibold text-red-400 hover:bg-red-500/10 hover:border-red-500/20 transition"
                                                >
                                                    🗑️ Delete
                                                </button>

                                                <button
                                                    type="button"
                                                    onClick={() =>
                                                        handleOpenShare(
                                                            credential.id
                                                        )
                                                    }
                                                    className="h-9 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-xs font-semibold text-cyan-400 hover:bg-cyan-500/20 transition"
                                                >
                                                    🔗 Share
                                                </button>

                                            </div>

                                        </div>

                                    )
                                )}

                            </div>

                        )}

                    </div>

                </div>

                {/* =====================================================
                    SHARED WITH ME
                ====================================================== */}

                <div className="mt-6 bg-slate-900/70 backdrop-blur-xl border border-slate-800 rounded-3xl p-6 shadow-2xl">

                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">

                        <div className="flex items-center gap-3">

                            <div className="w-10 h-10 rounded-xl bg-cyan-500/10 flex items-center justify-center">
                                🤝
                            </div>

                            <div>

                                <h2 className="text-xl font-bold">
                                    Shared With Me
                                </h2>

                                <p className="text-slate-500 text-xs mt-1">
                                    Credentials other users have shared with you
                                </p>

                            </div>

                        </div>

                        <div className="px-3 py-1.5 rounded-full bg-slate-950 border border-slate-800 text-xs text-slate-400">
                            {sharedCredentials.length}
                        </div>

                    </div>

                    {sharedLoading ? (

                        <div className="flex flex-col items-center justify-center py-12">

                            <div className="w-8 h-8 border-2 border-slate-700 border-t-cyan-400 rounded-full animate-spin"></div>

                            <p className="text-sm text-slate-500 mt-4">
                                Loading shared credentials...
                            </p>

                        </div>

                    ) : sharedCredentials.length === 0 ? (

                        <div className="text-center py-12 px-5 rounded-2xl border border-dashed border-slate-800 bg-slate-950/30">

                            <div className="text-4xl mb-4">
                                🤝
                            </div>

                            <h3 className="font-semibold text-slate-300">
                                No shared credentials
                            </h3>

                            <p className="text-xs text-slate-600 mt-2">
                                Credentials shared with you will appear here.
                            </p>

                        </div>

                    ) : (

                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

                            {sharedCredentials.map(
                                (sharedCredential) => {

                                    const isViewOnly =
                                        sharedCredential.permissionLevel ===
                                        "VIEW_ONLY";

                                    const canEdit =
                                        sharedCredential.permissionLevel ===
                                            "EDIT_ACCESS" ||
                                        sharedCredential.permissionLevel ===
                                            "FULL_MANAGEMENT";

                                    const isExpired =
                                        sharedCredential.expirationDate &&
                                        new Date(
                                            sharedCredential.expirationDate
                                        ) <= new Date();

                                    return (

                                        <div
                                            key={
                                                sharedCredential.shareId
                                            }
                                            className="rounded-2xl bg-slate-950/50 border border-slate-800 p-5 hover:border-cyan-500/20 transition"
                                        >

                                            {/* SHARED CARD HEADER */}

                                            <div className="flex items-start justify-between gap-3 mb-4">

                                                <div className="flex items-center gap-3 min-w-0">

                                                    <div className="w-10 h-10 shrink-0 rounded-xl bg-gradient-to-br from-cyan-500/10 to-blue-500/10 border border-cyan-500/10 flex items-center justify-center">
                                                        🔗
                                                    </div>

                                                    <div className="min-w-0">

                                                        <h3 className="font-semibold text-slate-200 truncate">
                                                            {
                                                                sharedCredential.title
                                                            }
                                                        </h3>

                                                        <p className="text-xs text-slate-600 mt-1">
                                                            Shared credential
                                                        </p>

                                                    </div>

                                                </div>

                                            </div>

                                            {/* PERMISSION */}

                                            <div className="mb-4">

                                                <span
                                                    className={`inline-flex items-center px-3 py-1.5 rounded-full border text-xs font-semibold ${getPermissionClass(
                                                        sharedCredential.permissionLevel
                                                    )}`}
                                                >
                                                    {getPermissionLabel(
                                                        sharedCredential.permissionLevel
                                                    )}
                                                </span>

                                                <p className="text-xs text-slate-600 mt-2">
                                                    {getPermissionDescription(
                                                        sharedCredential.permissionLevel
                                                    )}
                                                </p>

                                            </div>

                                            {/* SHARED DETAILS */}

                                            <div className="space-y-2">

                                                <div className="flex items-center justify-between gap-3 rounded-lg bg-slate-900/70 px-3 py-2">

                                                    <span className="text-xs text-slate-600">
                                                        Owner
                                                    </span>

                                                    <span className="text-xs text-slate-400 truncate">
                                                        {
                                                            sharedCredential.ownerUsername
                                                        }
                                                    </span>

                                                </div>

                                                <div className="flex items-center justify-between gap-3 rounded-lg bg-slate-900/70 px-3 py-2">

                                                    <span className="text-xs text-slate-600">
                                                        Username
                                                    </span>

                                                    <span className="text-xs text-slate-400 truncate">
                                                        {
                                                            sharedCredential.username
                                                        }
                                                    </span>

                                                </div>

                                                {sharedCredential.expirationDate && (

                                                    <div className="flex items-center justify-between gap-3 rounded-lg bg-slate-900/70 px-3 py-2">

                                                        <span className="text-xs text-slate-600">
                                                            Expires
                                                        </span>

                                                        <span
                                                            className={`text-xs ${
                                                                isExpired
                                                                    ? "text-red-400"
                                                                    : "text-slate-400"
                                                            }`}
                                                        >
                                                            {new Date(
                                                                sharedCredential.expirationDate
                                                            ).toLocaleString()}
                                                        </span>

                                                    </div>

                                                )}

                                            </div>

                                            {/* ACTIONS */}

                                            {!isExpired && (

                                                <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 mt-4">

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleViewSharedCredential(
                                                                sharedCredential
                                                            )
                                                        }
                                                        className="flex-1 h-9 rounded-lg bg-cyan-500/10 border border-cyan-500/20 text-xs font-semibold text-cyan-400 hover:bg-cyan-500/20 transition"
                                                    >
                                                        👁️ View
                                                    </button>

                                                    {canEdit && (

                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleEditSharedCredential(
                                                                    sharedCredential
                                                                )
                                                            }
                                                            className="flex-1 h-9 rounded-lg bg-slate-800 border border-slate-700 text-xs font-semibold text-slate-300 hover:bg-slate-700 hover:text-white transition"
                                                        >
                                                            ✏️ Edit
                                                        </button>

                                                    )}

                                                    {sharedCredential.permissionLevel ===
                                                        "FULL_MANAGEMENT" && (

                                                        <button
                                                            type="button"
                                                            onClick={() =>
                                                                handleOpenManageAccess(
                                                                    sharedCredential
                                                                )
                                                            }
                                                            className="flex-1 h-9 rounded-lg bg-violet-500/10 border border-violet-500/20 text-xs font-semibold text-violet-400 hover:bg-violet-500/20 transition"
                                                        >
                                                            ⚙️ Manage Access
                                                        </button>

                                                    )}

                                                </div>

                                            )}

                                            {/* VIEW ONLY MESSAGE */}

                                            {isViewOnly &&
                                                !isExpired && (

                                                    <p className="text-xs text-slate-600 mt-3 text-center">
                                                        View Only access — editing is not permitted.
                                                    </p>

                                                )}

                                            {/* EXPIRED MESSAGE */}

                                            {isExpired && (

                                                <div className="mt-4 rounded-lg bg-red-500/5 border border-red-500/10 px-3 py-2 text-center">

                                                    <span className="text-xs text-red-400">
                                                        ⚠ Shared access has expired
                                                    </span>

                                                </div>

                                            )}

                                        </div>

                                    );
                                }
                            )}

                        </div>

                    )}

                </div>

                {/* =====================================================
                    SHARE CREDENTIAL MODAL
                ====================================================== */}

                {sharingCredentialId !== null && (

                    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">

                        <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl">

                            {/* HEADER */}

                            <div className="flex items-center justify-between mb-6">

                                <div>

                                    <h2 className="text-xl font-bold">
                                        Share Credential
                                    </h2>

                                    <p className="text-xs text-slate-500 mt-1">
                                        Give another SecureVault user access to this credential.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    onClick={
                                        handleCloseShare
                                    }
                                    className="w-9 h-9 rounded-lg bg-slate-800 text-slate-400 hover:text-white hover:bg-slate-700 transition"
                                >
                                    ✕
                                </button>

                            </div>

                            <form
                                onSubmit={
                                    handleShareCredential
                                }
                                className="space-y-5"
                            >

                                {/* EMAIL */}

                                <div>

                                    <label
                                        htmlFor="sharingEmail"
                                        className="block text-sm font-medium text-slate-300 mb-2"
                                    >
                                        Shared User Email
                                    </label>

                                    <input
                                        id="sharingEmail"
                                        type="email"
                                        value={sharingEmail}
                                        onChange={(event) =>
                                            setSharingEmail(
                                                event.target.value
                                            )
                                        }
                                        placeholder="userb@example.com"
                                        className="w-full h-12 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white placeholder-slate-600 outline-none focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                        required
                                    />

                                    <p className="text-xs text-slate-600 mt-2">
                                        The user must already have a SecureVault account.
                                    </p>

                                </div>

                                {/* PERMISSION */}

                                <div>

                                    <label
                                        htmlFor="sharingPermission"
                                        className="block text-sm font-medium text-slate-300 mb-2"
                                    >
                                        Permission Level
                                    </label>

                                    <select
                                        id="sharingPermission"
                                        value={
                                            sharingPermission
                                        }
                                        onChange={(event) =>
                                            setSharingPermission(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full h-12 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                    >

                                        <option value="VIEW_ONLY">
                                            View Only
                                        </option>

                                        <option value="EDIT_ACCESS">
                                            Edit Access
                                        </option>

                                        <option value="FULL_MANAGEMENT">
                                            Full Management
                                        </option>

                                    </select>

                                    <div className="mt-3 rounded-xl bg-slate-950/70 border border-slate-800 p-3">

                                        <p className="text-xs text-slate-400">

                                            {getPermissionDescription(
                                                sharingPermission
                                            )}

                                        </p>

                                    </div>

                                </div>

                                {/* EXPIRATION */}

                                <div>

                                    <label
                                        htmlFor="sharingExpiration"
                                        className="block text-sm font-medium text-slate-300 mb-2"
                                    >
                                        Expiration Date
                                        <span className="text-slate-600 font-normal">
                                            {" "}
                                            (Optional)
                                        </span>
                                    </label>

                                    <input
                                        id="sharingExpiration"
                                        type="datetime-local"
                                        value={
                                            sharingExpiration
                                        }
                                        onChange={(event) =>
                                            setSharingExpiration(
                                                event.target
                                                    .value
                                            )
                                        }
                                        min={
                                            new Date(
                                                Date.now() -
                                                    new Date().getTimezoneOffset() *
                                                        60000
                                            )
                                                .toISOString()
                                                .slice(
                                                    0,
                                                    16
                                                )
                                        }
                                        className="w-full h-12 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/20"
                                    />

                                    <p className="text-xs text-slate-600 mt-2">
                                        Leave empty for access without an expiration date.
                                    </p>

                                </div>

                                {/* BUTTONS */}

                                <div className="flex gap-3 pt-2">

                                    <button
                                        type="submit"
                                        className="flex-1 h-12 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 font-semibold text-sm shadow-lg shadow-cyan-500/15 hover:shadow-cyan-500/30 transition"
                                    >
                                        🔗 Share Credential
                                    </button>

                                    <button
                                        type="button"
                                        onClick={
                                            handleCloseShare
                                        }
                                        className="px-5 h-12 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 font-semibold text-sm hover:bg-slate-700 transition"
                                    >
                                        Cancel
                                    </button>

                                </div>

                            </form>

                        </div>

                    </div>

                )}

                {/* =====================================================
                    EDIT SHARED CREDENTIAL MODAL
                ====================================================== */}

                {editingSharedId !== null && (

                    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">

                        <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl">

                            <div className="flex items-center justify-between mb-6">

                                <div>

                                    <h2 className="text-xl font-bold">
                                        Edit Shared Credential
                                    </h2>

                                    <p className="text-xs text-slate-500 mt-1">
                                        Your permission allows you to update this credential.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    onClick={
                                        handleCancelSharedEdit
                                    }
                                    className="w-9 h-9 rounded-lg bg-slate-800 text-slate-400 hover:text-white hover:bg-slate-700 transition"
                                >
                                    ✕
                                </button>

                            </div>

                            <form
                                onSubmit={
                                    handleUpdateSharedCredential
                                }
                                className="space-y-4"
                            >

                                {/* TITLE */}

                                <div>

                                    <label className="block text-xs font-medium text-slate-400 mb-2">
                                        Service / Title
                                    </label>

                                    <input
                                        type="text"
                                        value={sharedTitle}
                                        onChange={(event) =>
                                            setSharedTitle(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-cyan-400"
                                    />

                                </div>

                                {/* USERNAME */}

                                <div>

                                    <label className="block text-xs font-medium text-slate-400 mb-2">
                                        Username
                                    </label>

                                    <input
                                        type="text"
                                        value={sharedUsername}
                                        onChange={(event) =>
                                            setSharedUsername(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-cyan-400"
                                    />

                                </div>

                                {/* PASSWORD */}

                                <div>

                                    <label className="block text-xs font-medium text-slate-400 mb-2">
                                        Password
                                    </label>

                                    <input
                                        type="text"
                                        value={sharedPassword}
                                        onChange={(event) =>
                                            setSharedPassword(
                                                event.target
                                                    .value
                                            )
                                        }
                                        className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-cyan-400"
                                    />

                                </div>

                                {/* BUTTONS */}

                                <div className="flex gap-3 pt-2">

                                    <button
                                        type="submit"
                                        className="flex-1 h-11 rounded-xl bg-gradient-to-r from-cyan-500 to-blue-600 font-semibold text-sm"
                                    >
                                        Update Credential
                                    </button>

                                    <button
                                        type="button"
                                        onClick={
                                            handleCancelSharedEdit
                                        }
                                        className="px-5 h-11 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 font-semibold text-sm"
                                    >
                                        Cancel
                                    </button>

                                </div>

                            </form>

                        </div>

                    </div>

                )}

                {/* =====================================================
                    MANAGE ACCESS MODAL
                ====================================================== */}

                {managingCredentialId !== null && (

                    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm p-4">

                        <div className="w-full max-w-2xl max-h-[90vh] overflow-y-auto bg-slate-900 border border-slate-800 rounded-3xl p-6 shadow-2xl">

                            <div className="flex items-center justify-between mb-6">

                                <div>

                                    <h2 className="text-xl font-bold">
                                        Manage Access
                                    </h2>

                                    <p className="text-xs text-slate-500 mt-1">
                                        Manage users who can access this credential.
                                    </p>

                                </div>

                                <button
                                    type="button"
                                    onClick={
                                        handleCloseManageAccess
                                    }
                                    className="w-9 h-9 rounded-lg bg-slate-800 text-slate-400 hover:text-white hover:bg-slate-700 transition"
                                >
                                    ✕
                                </button>

                            </div>


                            <div className="mb-6">

                                <div className="flex items-center justify-between mb-3">

                                    <h3 className="text-sm font-semibold text-slate-300">
                                        Current Access
                                    </h3>

                                    <span className="text-xs text-slate-600">
                                        {managedShares.length} user(s)
                                    </span>

                                </div>


                                {manageLoading ? (

                                    <div className="flex items-center justify-center py-8">

                                        <div className="w-8 h-8 border-2 border-slate-700 border-t-violet-400 rounded-full animate-spin"></div>

                                    </div>

                                ) : managedShares.length === 0 ? (

                                    <div className="rounded-xl border border-dashed border-slate-800 bg-slate-950/40 px-4 py-6 text-center">

                                        <p className="text-xs text-slate-500">
                                            No users currently have shared access.
                                        </p>

                                    </div>

                                ) : (

                                    <div className="space-y-2">

                                        {managedShares.map(
                                            (share) => (

                                                <div
                                                    key={
                                                        share.shareId
                                                    }
                                                    className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 rounded-xl bg-slate-950/60 border border-slate-800 p-4"
                                                >

                                                    <div className="min-w-0">

                                                        <p className="text-sm font-medium text-slate-300 truncate">
                                                            {share.sharedUserUsername ||
                                                                "User"}
                                                        </p>

                                                        <p className="text-xs text-slate-600 mt-1">
                                                            {getPermissionLabel(
                                                                share.permissionLevel
                                                            )}
                                                        </p>

                                                        {share.expirationDate && (

                                                            <p className="text-xs text-slate-600 mt-1">
                                                                Expires:{" "}
                                                                {new Date(
                                                                    share.expirationDate
                                                                ).toLocaleString()}
                                                            </p>

                                                        )}

                                                    </div>

                                                    <button
                                                        type="button"
                                                        onClick={() =>
                                                            handleManagerRemoveAccess(
                                                                share.sharedUserId
                                                            )
                                                        }
                                                        className="shrink-0 h-9 px-4 rounded-lg bg-red-500/5 border border-red-500/10 text-xs font-semibold text-red-400 hover:bg-red-500/10 transition"
                                                    >
                                                        🗑️ Remove
                                                    </button>

                                                </div>

                                            )
                                        )}

                                    </div>

                                )}

                            </div>


                            <div className="border-t border-slate-800 pt-6">

                                <h3 className="text-sm font-semibold text-slate-300 mb-4">
                                    Grant New Access
                                </h3>

                                <form
                                    onSubmit={
                                        handleManagerShare
                                    }
                                    className="space-y-4"
                                >

                                    <div>

                                        <label
                                            htmlFor="manageEmail"
                                            className="block text-xs font-medium text-slate-400 mb-2"
                                        >
                                            User Email
                                        </label>

                                        <input
                                            id="manageEmail"
                                            type="email"
                                            value={manageEmail}
                                            onChange={(event) =>
                                                setManageEmail(
                                                    event.target.value
                                                )
                                            }
                                            placeholder="user@example.com"
                                            required
                                            className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white placeholder-slate-600 outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20"
                                        />

                                        <p className="text-xs text-slate-600 mt-2">
                                            The user must already have a SecureVault account.
                                        </p>

                                    </div>


                                    <div>

                                        <label
                                            htmlFor="managePermission"
                                            className="block text-xs font-medium text-slate-400 mb-2"
                                        >
                                            Permission Level
                                        </label>

                                        <select
                                            id="managePermission"
                                            value={
                                                managePermission
                                            }
                                            onChange={(event) =>
                                                setManagePermission(
                                                    event.target.value
                                                )
                                            }
                                            className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20"
                                        >

                                            <option value="VIEW_ONLY">
                                                View Only
                                            </option>

                                            <option value="EDIT_ACCESS">
                                                Edit Access
                                            </option>

                                            <option value="FULL_MANAGEMENT">
                                                Full Management
                                            </option>

                                        </select>

                                    </div>


                                    <div>

                                        <label
                                            htmlFor="manageExpiration"
                                            className="block text-xs font-medium text-slate-400 mb-2"
                                        >
                                            Expiration Date
                                            <span className="text-slate-600 font-normal">
                                                {" "}
                                                (Optional)
                                            </span>
                                        </label>

                                        <input
                                            id="manageExpiration"
                                            type="datetime-local"
                                            value={
                                                manageExpiration
                                            }
                                            onChange={(event) =>
                                                setManageExpiration(
                                                    event.target.value
                                                )
                                            }
                                            min={
                                                new Date(
                                                    Date.now() -
                                                        new Date().getTimezoneOffset() *
                                                            60000
                                                )
                                                    .toISOString()
                                                    .slice(0, 16)
                                            }
                                            className="w-full h-11 px-4 rounded-xl bg-slate-950 border border-slate-700 text-white outline-none focus:border-violet-400 focus:ring-2 focus:ring-violet-400/20"
                                        />

                                    </div>


                                    <div className="flex gap-3 pt-2">

                                        <button
                                            type="submit"
                                            className="flex-1 h-11 rounded-xl bg-gradient-to-r from-violet-500 to-blue-600 font-semibold text-sm shadow-lg shadow-violet-500/15 hover:shadow-violet-500/30 transition"
                                        >
                                            ➕ Grant Access
                                        </button>

                                        <button
                                            type="button"
                                            onClick={
                                                handleCloseManageAccess
                                            }
                                            className="px-5 h-11 rounded-xl bg-slate-800 border border-slate-700 text-slate-300 font-semibold text-sm hover:bg-slate-700 transition"
                                        >
                                            Close
                                        </button>

                                    </div>

                                </form>

                            </div>

                        </div>

                    </div>

                )}

                {/* =====================================================
                    FOOTER
                ====================================================== */}

                <div className="text-center mt-8">

                    <p className="text-xs text-slate-700">
                        🔐 SecureVault • Your credentials are protected
                    </p>

                </div>

            </div>

        </div>
    );
}

export default Vault;
