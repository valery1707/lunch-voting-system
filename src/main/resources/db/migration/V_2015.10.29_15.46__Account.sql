CREATE TABLE account (
		id         UUID NOT NULL CONSTRAINT pk$account PRIMARY KEY,
		login      TEXT NOT NULL,
		password   TEXT NOT NULL,
		first_name TEXT NOT NULL,
		last_name  TEXT NOT NULL
);
COMMENT ON TABLE account IS 'Accounts';

INSERT INTO account (id, login, password, first_name, last_name)
VALUES
		--password = md5('admin')
		('b2c0f756-bc2e-4352-90af-a36f9ab3fb46', 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Admin', 'Admin');
