CREATE TABLE account (
		id         UUID         NOT NULL CONSTRAINT pk$account PRIMARY KEY,
		login      VARCHAR(255) NOT NULL,
		password   VARCHAR(32)  NOT NULL,
		first_name VARCHAR(255) NOT NULL,
		last_name  VARCHAR(255) NOT NULL
);
COMMENT ON TABLE account IS 'Accounts';

INSERT INTO account (id, login, password, first_name, last_name)
VALUES
		--password = md5('admin')
		('b2c0f756-bc2e-4352-90af-a36f9ab3fb46', 'admin', '21232f297a57a5a743894a0e4a801fc3', 'Admin', 'Admin');
