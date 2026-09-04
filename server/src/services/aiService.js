const OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions";

/*
 * Get today's date in India.
 * Returns: YYYY-MM-DD
 */
function getIndiaToday() {
    const formatter = new Intl.DateTimeFormat("en-CA", {
        timeZone: "Asia/Kolkata",
        year: "numeric",
        month: "2-digit",
        day: "2-digit"
    });

    return formatter.format(new Date());
}

/*
 * Return YYYY-MM-DD after adding days.
 */
function addDays(dateString, days) {
    const [year, month, day] = dateString.split("-").map(Number);

    const date = new Date(
        Date.UTC(year, month - 1, day)
    );

    date.setUTCDate(date.getUTCDate() + days);

    return date.toISOString().split("T")[0];
}

/*
 * Find the next occurrence of a weekday.
 */
function getNextWeekdayDate(input, todayString) {

    const text = input.toLowerCase();

    const weekdays = {
        sunday: 0,
        monday: 1,
        tuesday: 2,
        wednesday: 3,
        thursday: 4,
        friday: 5,
        saturday: 6
    };

    const [year, month, day] =
        todayString.split("-").map(Number);

    const today = new Date(
        Date.UTC(year, month - 1, day)
    );

    const currentDay = today.getUTCDay();

    for (const [weekdayName, targetDay] of Object.entries(weekdays)) {

        const normalMention =
            text.includes(`for ${weekdayName}`) ||
            text.includes(`on ${weekdayName}`) ||
            text.includes(`by ${weekdayName}`);

        const nextMention =
            text.includes(`next ${weekdayName}`);

        if (normalMention || nextMention) {

            let daysUntil = targetDay - currentDay;

            // Always use a future occurrence
            if (daysUntil <= 0) {
                daysUntil += 7;
            }

            return addDays(todayString, daysUntil);
        }
    }

    return null;
}

/*
 * Handle today / tomorrow / day after tomorrow.
 */
function getRelativeDate(input, todayString) {

    const text = input.toLowerCase();

    if (text.includes("day after tomorrow")) {
        return addDays(todayString, 2);
    }

    if (text.includes("tomorrow")) {
        return addDays(todayString, 1);
    }

    if (text.includes("today")) {
        return todayString;
    }

    return null;
}

/*
 * Extract a deterministic date from user input.
 */
function extractDueDate(input, todayString) {

    const relativeDate = getRelativeDate(
        input,
        todayString
    );

    if (relativeDate) {
        return relativeDate;
    }

    const weekdayDate = getNextWeekdayDate(
        input,
        todayString
    );

    if (weekdayDate) {
        return weekdayDate;
    }

    return null;
}


export async function generateTaskFromAI(userInput) {

    const today = getIndiaToday();

    /*
     * Express calculates the date.
     * AI does NOT calculate relative dates.
     */
    const resolvedDate = extractDueDate(
        userInput,
        today
    );

    const resolvedDueDate = resolvedDate || "";

    console.log("--------------------------------");
    console.log("Today:", today);
    console.log("Detected date:", resolvedDate);
    console.log("Resolved due date:", resolvedDueDate);
    console.log("--------------------------------");


    const response = await fetch(
        OPENROUTER_URL,
        {
            method: "POST",

            headers: {
                "Authorization":
                    `Bearer ${process.env.OPENROUTER_API_KEY}`,

                "Content-Type":
                    "application/json"
            },

            body: JSON.stringify({

                model: "openai/gpt-4o-mini",

                messages: [

                    {
                        role: "system",

                        content: `
You are an AI task management assistant.

Today's date is ${today}.
Timezone: Asia/Kolkata.

The application has already calculated the due date.

Application-calculated due date:
${resolvedDueDate}

IMPORTANT:
- If an application-calculated due date is provided,
  return EXACTLY that date.
- NEVER modify or recalculate it.
- The dueDate must be in YYYY-MM-DD format.
- If no due date is detected, return an empty string.

TASK RULES:
- Create a short and clear task title.
- Put useful details in description.
- Priority must be exactly LOW, MEDIUM, or HIGH.
- If priority is not mentioned, use MEDIUM.
- Put names, recipients, people, or useful context
  in additionalInfo.
- Do not invent important information.
`
                    },

                    {
                        role: "user",
                        content: userInput
                    }

                ],

                response_format: {

                    type: "json_schema",

                    json_schema: {

                        name: "task",

                        strict: true,

                        schema: {

                            type: "object",

                            properties: {

                                title: {
                                    type: "string"
                                },

                                description: {
                                    type: "string"
                                },

                                dueDate: {
                                    type: "string"
                                },

                                priority: {
                                    type: "string",

                                    enum: [
                                        "LOW",
                                        "MEDIUM",
                                        "HIGH"
                                    ]
                                },

                                additionalInfo: {
                                    type: "string"
                                }

                            },

                            required: [
                                "title",
                                "description",
                                "dueDate",
                                "priority",
                                "additionalInfo"
                            ],

                            additionalProperties: false
                        }
                    }
                }
            })
        }
    );


    const data = await response.json();


    if (!response.ok) {

        console.error(
            "OpenRouter error:",
            data
        );

        throw new Error(
            data?.error?.message ||
            "Failed to generate task using AI"
        );
    }


    const content =
        data?.choices?.[0]?.message?.content;


    if (!content) {
        throw new Error(
            "AI returned an empty response"
        );
    }


    const task = JSON.parse(content);


    /*
     * Final safety check.
     * Express has the final authority over relative dates.
     */
    if (resolvedDueDate) {
        task.dueDate = resolvedDueDate;
    }


    return task;
}
export async function generateSubtasksFromAI(taskTitle, taskDescription) {

    const response = await fetch(
        OPENROUTER_URL,
        {
            method: "POST",

            headers: {
                "Authorization":
                    `Bearer ${process.env.OPENROUTER_API_KEY}`,

                "Content-Type":
                    "application/json"
            },

            body: JSON.stringify({

                model: "openai/gpt-4o-mini",

                messages: [

                    {
                        role: "system",

                        content: `
You are an AI task management assistant.

Break the given task into a small number of practical subtasks.

Rules:
- Create 3 to 7 subtasks.
- Each subtask should be clear and actionable.
- Keep each subtask short.
- Do not repeat the main task.
- Arrange subtasks in a logical order.
- Do not invent unnecessary information.
`
                    },

                    {
                        role: "user",

                        content: `
Task Title:
${taskTitle}

Task Description:
${taskDescription || "No description provided."}
`
                    }

                ],

                response_format: {

                    type: "json_schema",

                    json_schema: {

                        name: "subtasks",

                        strict: true,

                        schema: {

                            type: "object",

                            properties: {

                                subtasks: {

                                    type: "array",

                                    items: {

                                        type: "object",

                                        properties: {

                                            title: {
                                                type: "string"
                                            }

                                        },

                                        required: [
                                            "title"
                                        ],

                                        additionalProperties: false
                                    }
                                }

                            },

                            required: [
                                "subtasks"
                            ],

                            additionalProperties: false
                        }
                    }
                }
            })
        }
    );

    const data = await response.json();

    if (!response.ok) {

        console.error(
            "OpenRouter subtask error:",
            data
        );

        throw new Error(
            data?.error?.message ||
            "Failed to generate subtasks using AI"
        );
    }

    const content =
        data?.choices?.[0]?.message?.content;

    if (!content) {

        throw new Error(
            "AI returned an empty subtask response"
        );
    }

    return JSON.parse(content);
}