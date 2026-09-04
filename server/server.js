import express from "express";
import cors from "cors";
import dotenv from "dotenv";

import aiRoutes from "./src/routes/aiRoutes.js";

dotenv.config();

const app = express();

app.use(cors());
app.use(express.json());

// Health check
app.get("/", (req, res) => {
    res.json({
        success: true,
        message: "AI Task Manager Server is running"
    });
});

// AI routes
app.use("/api/ai", aiRoutes);

const PORT = process.env.PORT || 3000;

app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on port ${PORT}`);
});