import express from "express";

import {
    createTaskWithAI,
    createSubtasksWithAI
} from "../controllers/aiController.js";

const router = express.Router();

router.post(
    "/create-task",
    createTaskWithAI
);

router.post(
    "/create-subtasks",
    createSubtasksWithAI
);

export default router;