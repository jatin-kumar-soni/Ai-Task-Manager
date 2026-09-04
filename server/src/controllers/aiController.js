import { generateTaskFromAI,generateSubtasksFromAI } from "../services/aiService.js";

export async function createTaskWithAI(req, res) {
    try {
        const { input } = req.body;

        if (!input || !input.trim()) {
            return res.status(400).json({
                success: false,
                message: "Task description is required"
            });
        }

        const task = await generateTaskFromAI(input.trim());

        return res.status(200).json({
            success: true,
            task
        });

    } catch (error) {
        console.error("AI Controller Error:", error);

        return res.status(500).json({
            success: false,
            message: error.message || "AI task generation failed"
        });
    }
}
export async function createSubtasksWithAI(req, res) {

    try {

        const {
            title,
            description
        } = req.body;

        if (!title || !title.trim()) {

            return res.status(400).json({
                success: false,
                message: "Task title is required"
            });
        }

        const result =
            await generateSubtasksFromAI(
                title.trim(),
                description?.trim() || ""
            );

        return res.status(200).json({
            success: true,
            subtasks: result.subtasks
        });

    } catch (error) {

        console.error(
            "Subtask AI Controller Error:",
            error
        );

        return res.status(500).json({
            success: false,
            message:
                error.message ||
                "AI subtask generation failed"
        });
    }
}