CREATE TABLE restaurant (
		id   UUID         NOT NULL CONSTRAINT pk$restaurant PRIMARY KEY,
		name VARCHAR(255) NOT NULL
);

CREATE TABLE dish (
		id            UUID         NOT NULL CONSTRAINT pk$dish PRIMARY KEY,
		restaurant_id UUID         NOT NULL CONSTRAINT fk$dish$restaurant_id REFERENCES restaurant,
		name          VARCHAR(255) NOT NULL,
		price         DOUBLE       NOT NULL
);

INSERT INTO restaurant (id, name) VALUES
		('60d4f411-4cff-4f60-b392-46bed14c5f86', 'Moe''s Bar'),
		('78a9353f-7e08-40a6-ad70-af2664a37a36', 'Hell''s kitchen');
INSERT INTO dish (id, restaurant_id, name, price) VALUES
		('6b2edfa5-0894-4fca-aed0-511171f650f5', '60d4f411-4cff-4f60-b392-46bed14c5f86', 'Beer "Duff" 0.5', 1.0),
		('f5135643-285e-4849-ad4f-0b652a85dcbc', '60d4f411-4cff-4f60-b392-46bed14c5f86', 'Beer "Duff" 0.7', 3.0),
		('8db4a16c-78db-4810-95d8-a67d773a27b7', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'Warm Artichoke Dip', 7.5),
		('e09ceaa2-21e4-4097-bade-8e5a4f7a0f4e', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'Mini Crab Cake', 8.5),
		('646de66a-9eb2-4710-ab26-0c7f16b08b3a', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'BBQ Pork Nachos', 11.95),
		('d5cabfdc-d005-44ac-8b1b-0e8b4086f5ff', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'Chicken Wings', 11.95),
		('6c38892d-4a89-40dc-8159-2bab26b48a7a', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'Grilled Cheese', 10.95),
		('152372b2-69cc-4940-ab0c-7c2de391894f', '78a9353f-7e08-40a6-ad70-af2664a37a36', 'Steak and Eggs', 15.95);
