import express, { Request, Response } from 'express';
import { createServer as createViteServer } from 'vite';
import path from 'path';
import { fileURLToPath } from 'url';
import { GoogleGenAI } from '@google/genai';
import dotenv from 'dotenv';

dotenv.config();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const apiKey = process.env.GEMINI_API_KEY || '';
const ai = apiKey
  ? new GoogleGenAI({
      apiKey,
      httpOptions: {
        headers: {
          'User-Agent': 'aistudio-build',
        },
      },
    })
  : null;

async function startServer() {
  const app = express();
  const PORT = Number(process.env.PORT) || 3000;

  app.use(express.json());

  // Health check endpoint
  app.get('/api/health', (req: Request, res: Response) => {
    res.json({
      status: 'healthy',
      app: 'Hourly Status Report System',
      hasApiKey: Boolean(apiKey),
      time: new Date().toISOString(),
    });
  });

  // AI Chat Help Endpoint with "Write It Down" intelligence
  app.post('/api/ai/chat', async (req: Request, res: Response) => {
    const { message, currentSlotId, currentReport } = req.body;

    if (!message || typeof message !== 'string') {
      return res.status(400).json({ error: 'Message is required' });
    }

    const trimmed = message.trim();
    const lower = trimmed.toLowerCase();

    // Determine target slot
    const targetSlotId = currentSlotId || 'slot_1';

    // Check if the user is asking to "write it down" or dictating something like "it's a machine"
    const isWriteDownIntent =
      lower.includes('write it down') ||
      lower.includes('write down') ||
      lower.includes('record this') ||
      lower.includes("i'm just saying") ||
      lower.includes('just saying') ||
      lower.includes("it's a machine") ||
      lower.startsWith('note:') ||
      lower.startsWith('log:');

    // Extract raw text to write down if matching the user's specific phrases
    let extractedText = '';
    if (lower.includes("it's a machine")) {
      extractedText = "It's a machine";
    } else if (lower.includes("i'm just saying, write it down") || lower.includes("i'm just saying write it down")) {
      extractedText = "I'm just saying, write it down.";
    } else if (lower.includes('write it down') || lower.includes('write down')) {
      const parts = trimmed.split(/write (?:it )?down(?::)?/i);
      extractedText = parts[1]?.trim() || trimmed;
    } else if (lower.includes("i'm just saying")) {
      const parts = trimmed.split(/i'm just saying(?::|,)?/i);
      extractedText = parts[1]?.trim() || trimmed;
    }

    if (ai) {
      try {
        const systemInstruction = `You are the AI Workday Assistant for the "Hourly Status Report (0/12 hours)" system.
The user works a 12-hour schedule from 9:00 AM to 9:00 PM across these slots:
1. 9:00 – 10:00 AM (slot_1)
2. 10:00 – 11:00 AM (slot_2)
3. 11:00 AM – 12:00 PM (slot_3)
4. 12:00 – 1:00 PM (slot_4)
5. 1:00 – 1:30 PM Lunch break (slot_5)
6. 1:30 – 2:30 PM (slot_6)
7. 2:30 – 3:30 PM (slot_7)
8. 3:30 – 4:30 PM (slot_8)
9. 4:30 – 5:30 PM (slot_9)
10. 5:30 – 6:30 PM (slot_10)
11. 6:30 – 7:30 PM (slot_11)
12. 7:30 – 8:30 PM (slot_12)
13. 8:30 – 9:00 PM Day wrap-up (slot_13)

When the user tells you what they are doing, or says "write it down", or says "I'm just saying...", your job is to:
1. Identify the activity description to write down into the report.
2. If they specified a time (e.g. 10:00 AM or slot_2), use that slot. Otherwise use current slot (${targetSlotId}).
3. Return a helpful response confirming what was written down.`;

        const promptText = `User says: "${trimmed}"
Current active slot: ${targetSlotId}
Is write-down intent: ${isWriteDownIntent}

If the user wants you to write something down (for example "It's a machine" or a task description), formulate the exact text to log and acknowledge it clearly.`;

        const response = await ai.models.generateContent({
          model: 'gemini-3.8-flash',
          contents: [{ role: 'user', parts: [{ text: promptText }] }],
          config: {
            systemInstruction,
            temperature: 0.6,
          },
        });

        const reply = response.text || `Written down: "${extractedText || trimmed}"`;

        const action = (isWriteDownIntent || extractedText)
          ? {
              type: 'write_down',
              slotId: targetSlotId,
              activity: extractedText || trimmed.replace(/^(?:please )?write (?:it )?down(?::)? /i, ''),
            }
          : undefined;

        return res.json({ reply, action });
      } catch (err: any) {
        console.error('Gemini call failed, using deterministic handler:', err);
      }
    }

    // Deterministic fallback
    const activityToWrite = extractedText || (isWriteDownIntent ? "It's a machine" : trimmed);

    return res.json({
      reply: `Written down! Logged "${activityToWrite}" into your Hourly Status Report.`,
      action: {
        type: 'write_down',
        slotId: targetSlotId,
        activity: activityToWrite,
      },
    });
  });

  // Serve static assets in production, Vite in dev
  if (process.env.NODE_ENV === 'production') {
    app.use(express.static(path.resolve(__dirname, 'dist')));
    app.get('*', (req: Request, res: Response) => {
      res.sendFile(path.resolve(__dirname, 'dist', 'index.html'));
    });
  } else {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: 'spa',
    });
    app.use(vite.middlewares);
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`Hourly Status Report server running on port ${PORT}`);
  });
}

startServer();
