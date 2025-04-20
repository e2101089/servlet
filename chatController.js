const express = require('express');
const router = express.Router();
const db = require('./db');

// Get chat messages between two users
router.get('/:from/:to', async (req, res) => {
    const { from, to } = req.params;
    
    try {
        const [messages] = await db.query(
            `SELECT m.*, u1.name as sender_name, u2.name as receiver_name 
             FROM messages m
             JOIN users u1 ON m.sender_id = u1.id
             JOIN users u2 ON m.receiver_id = u2.id
             WHERE (m.sender_id = ? AND m.receiver_id = ?) 
             OR (m.sender_id = ? AND m.receiver_id = ?)
             ORDER BY m.timestamp ASC`,
            [from, to, to, from]
        );

        return res.status(200).json({
            success: true,
            data: messages
        });
    } catch (err) {
        console.error('Error fetching messages:', err);
        return res.status(500).json({
            success: false,
            message: 'Error fetching messages',
            error: err.message
        });
    }
});

// Send a message
router.post('/', async (req, res) => {
    const { sender_id, receiver_id, content } = req.body;

    if (!sender_id || !receiver_id || !content) {
        return res.status(400).json({
            success: false,
            message: 'All fields are required'
        });
    }

    try {
        // Verify both users exist
        const [sender] = await db.query('SELECT id FROM users WHERE id = ?', [sender_id]);
        const [receiver] = await db.query('SELECT id FROM users WHERE id = ?', [receiver_id]);

        if (!sender || sender.length === 0 || !receiver || receiver.length === 0) {
            return res.status(404).json({
                success: false,
                message: 'One or both users not found'
            });
        }

        // Insert the message
        const [result] = await db.query(
            'INSERT INTO messages (sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, NOW())',
            [sender_id, receiver_id, content]
        );

        return res.status(201).json({
            success: true,
            message: 'Message sent successfully',
            data: {
                id: result.insertId,
                sender_id,
                receiver_id,
                content,
                timestamp: new Date()
            }
        });
    } catch (err) {
        console.error('Error sending message:', err);
        return res.status(500).json({
            success: false,
            message: 'Error sending message',
            error: err.message
        });
    }
});

// Get user's chat list (people they've chatted with)
router.get('/users/:userId/chats', async (req, res) => {
    const { userId } = req.params;

    try {
        const [chats] = await db.query(
            `SELECT DISTINCT 
                CASE 
                    WHEN m.sender_id = ? THEN m.receiver_id 
                    ELSE m.sender_id 
                END as user_id,
                u.name,
                u.email,
                MAX(m.timestamp) as last_message_time
             FROM messages m
             JOIN users u ON (m.sender_id = u.id OR m.receiver_id = u.id) AND u.id != ?
             WHERE m.sender_id = ? OR m.receiver_id = ?
             GROUP BY user_id, u.name, u.email
             ORDER BY last_message_time DESC`,
            [userId, userId, userId, userId]
        );

        return res.status(200).json({
            success: true,
            data: chats
        });
    } catch (err) {
        console.error('Error fetching chat list:', err);
        return res.status(500).json({
            success: false,
            message: 'Error fetching chat list',
            error: err.message
        });
    }
});

module.exports = router; 