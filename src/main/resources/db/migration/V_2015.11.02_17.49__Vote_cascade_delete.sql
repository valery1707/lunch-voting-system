ALTER TABLE vote DROP CONSTRAINT fk$vote$account_id;
ALTER TABLE vote DROP CONSTRAINT fk$vote$restaurant_id;

ALTER TABLE vote ADD CONSTRAINT fk$vote$account_id FOREIGN KEY (account_id) REFERENCES account ON DELETE CASCADE;
ALTER TABLE vote ADD CONSTRAINT fk$vote$restaurant_id FOREIGN KEY (restaurant_id) REFERENCES restaurant ON DELETE CASCADE;
