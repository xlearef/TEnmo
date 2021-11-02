TRUNCATE accounts, transfers, users CASCADE;

INSERT INTO users (user_id, username, password_hash)
VALUES(7005, 'Testuser', '$2a$10$27aACnrZdrtAfde0Riw.XuClGV/rSv0qiw6pk0w32yNtx3MDdHBwC'),
(7006, 'Testuser2', '$2a$10$27aACnrZdrtAfde0Riw.XuClGV/rSv0qiw6pk0w32yNtx3MDdHBwC');

INSERT INTO accounts (account_id, user_id, balance)
VALUES(8005, 7005, 15000),(8006, 7006, 15000);

INSERT INTO transfers (transfer_id, transfer_type_id, transfer_status_id, account_from, account_to, amount)
VALUES (9005, 2, 2, 8005, 8006, 200.00);

