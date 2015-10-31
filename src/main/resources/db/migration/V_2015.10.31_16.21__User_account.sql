ALTER TABLE account ADD is_admin BOOLEAN;
UPDATE account SET is_admin = true;
ALTER TABLE account ALTER COLUMN is_admin SET NOT NULL;

ALTER TABLE account ADD CONSTRAINT uq$account$login UNIQUE (login);

INSERT INTO account (id, login, password, first_name, last_name, is_admin)
VALUES
		--password = md5('password one')
		('e9ad94ae-994a-436e-a99e-dec433af0089', 'user_1', '36076655a79f443195b0c302e98ee089', 'One', 'User', FALSE),
		--password = md5('password two')
		('da7be27f-fb01-4d35-9519-537c2500a182', 'user_2', '11cdac814cf9d1f3e14c44e42ee0b6b2', 'Two', 'User', FALSE);
