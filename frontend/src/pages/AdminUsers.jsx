import { useEffect, useState } from "react";

function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const token = localStorage.getItem("token");

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await fetch(
                "http://localhost:8080/api/admin/users",
                {
                    headers: {
                        Authorization: `Bearer ${token}`,
                    },
                }
            );

            const data = await response.json();

            if (!response.ok) {
                throw new Error(
                    data.message || "Failed to load users"
                );
            }

            setUsers(data);
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    return (
        <div className="min-h-screen bg-slate-950 text-white p-6">
            <div className="max-w-6xl mx-auto">

                <h1 className="text-3xl font-bold mb-2">
                    👤 Admin User Management
                </h1>

                <p className="text-slate-400 mb-8">
                    View registered users and their assigned roles.
                </p>

                {error && (
                    <div className="mb-6 rounded-xl border border-red-500/40 bg-red-500/10 p-4 text-red-300">
                        {error}
                    </div>
                )}

                {loading ? (
                    <div className="rounded-2xl bg-slate-900 p-8 text-center text-slate-400">
                        Loading users...
                    </div>
                ) : users.length === 0 ? (
                    <div className="rounded-2xl bg-slate-900 p-8 text-center text-slate-400">
                        No users found.
                    </div>
                ) : (
                    <div className="overflow-x-auto rounded-2xl border border-slate-800 bg-slate-900">
                        <table className="w-full text-left">
                            <thead className="border-b border-slate-800 bg-slate-950">
                                <tr>
                                    <th className="px-6 py-4">
                                        ID
                                    </th>
                                    <th className="px-6 py-4">
                                        Username
                                    </th>
                                    <th className="px-6 py-4">
                                        Email
                                    </th>
                                    <th className="px-6 py-4">
                                        Role
                                    </th>
                                </tr>
                            </thead>

                            <tbody>
                                {users.map((user) => (
                                    <tr
                                        key={user.id}
                                        className="border-b border-slate-800 hover:bg-slate-800/50"
                                    >
                                        <td className="px-6 py-4">
                                            {user.id}
                                        </td>

                                        <td className="px-6 py-4 font-medium">
                                            {user.username}
                                        </td>

                                        <td className="px-6 py-4 text-slate-300">
                                            {user.email}
                                        </td>

                                        <td className="px-6 py-4">
                                            <span className="rounded-full bg-cyan-500/20 px-3 py-1 text-sm text-cyan-300">
                                                {user.role}
                                            </span>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </div>
    );
}

export default AdminUsers;