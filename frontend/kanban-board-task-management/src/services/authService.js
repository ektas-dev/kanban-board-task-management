import axios from "axios";
import axiosInstance from "../api/axiosConfig";

export const login = async (loginData) => {
  console.log("1. login() called", loginData);

  const response = await axios.post(
    "http://localhost:8080/auth/login",
    loginData,
    {
      headers: {
        "Content-Type": "application/json",
      },
    },
  );

  console.log("2. POST response received", response);

  return response.data;
};

export const register = async (registerData) => {
  const response = await axiosInstance.post("/auth/register", registerData);

  return response.data;
};
