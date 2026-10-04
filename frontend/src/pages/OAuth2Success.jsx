import { useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";

function OAuth2Success() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    useEffect(() => {
        const token = searchParams.get("token");

        if (!token) {
            navigate("/login", { replace: true });
            return;
        }

        localStorage.setItem("token", token);

        navigate("/", { replace: true });
    }, [navigate, searchParams]);

    return (
        <div>
            <h2>Signing you in...</h2>
        </div>
    );
}

export default OAuth2Success;