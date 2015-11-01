CREATE TABLE vote (
		id            UUID      NOT NULL CONSTRAINT pk$vote PRIMARY KEY,
		date_time     TIMESTAMP NOT NULL,
		account_id    UUID      NOT NULL CONSTRAINT fk$vote$account_id REFERENCES account,
		restaurant_id UUID      NOT NULL CONSTRAINT fk$vote$restaurant_id REFERENCES restaurant,
-- 		CONSTRAINT uq$ UNIQUE (account_id, restaurant_id, TRUNC(date_time))--todo H2 does not support expressions in constraints
);

--Admin vote for "Hell's kitchen" today
INSERT INTO vote (id, date_time, account_id, restaurant_id) VALUES
		('7fd7315c-5cfb-4bb5-89eb-9e125d1ef0b6', now(), 'b2c0f756-bc2e-4352-90af-a36f9ab3fb46', '78a9353f-7e08-40a6-ad70-af2664a37a36');
