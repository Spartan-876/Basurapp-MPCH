import axios from "axios";

export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

const api = axios.create({
    baseURL: API_URL,
    headers: { "Content-Type": "application/json" },
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem("admin_token");
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

api.interceptors.response.use(
    (res) => res,
    (err) => {
        const url = err.config?.url || "";
        const isLoginRequest = url.includes("/api/admin/auth/login");

        if (
            !isLoginRequest &&
            (err.response?.status === 401 || err.response?.status === 403)
        ) {
            localStorage.removeItem("admin_token");
            localStorage.removeItem("admin_user");
            window.location.href = "/login";
        }
        return Promise.reject(err);
    },
);

export default api;
