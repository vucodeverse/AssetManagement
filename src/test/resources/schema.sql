CREATE TABLE warehouse
(
    id         INT IDENTITY (1,1) NOT NULL,
    name       VARCHAR(255)       NOT NULL,
    address    VARCHAR(255)       NULL,
    status     VARCHAR(40)        NOT NULL,
    manager_id INT                NULL,
    created_at DATETIME           NOT NULL,
    updated_at DATETIME           NULL,

    PRIMARY KEY (id)
--                            CONSTRAINT FK_warehouse_manager
--                                FOREIGN KEY (manager_id) REFERENCES users(user_id)
);

CREATE TABLE rack
(
    id           INT IDENTITY (1,1) NOT NULL,
    warehouse_id INT                NOT NULL,
    name         VARCHAR(255)       NOT NULL,
    description  VARCHAR(255)       NULL,
    status       VARCHAR(40)        NOT NULL,
    created_at   DATETIME           NOT NULL,
    updated_at   DATETIME           NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_rack_warehouse
        FOREIGN KEY (warehouse_id) REFERENCES warehouse (id)
);

CREATE TABLE shelf
(
    id               INT IDENTITY (1,1) NOT NULL,
    name             VARCHAR(255)       NOT NULL,
    current_capacity INT                NOT NULL DEFAULT 0,
    max_capacity     INT                NOT NULL,
    description      VARCHAR(255)       NULL,
    rack_id          INT                NOT NULL,
    asset_type_id    INT                NULL,
    status           VARCHAR(255)       NULL,

    created_at       DATETIME           NOT NULL,
    updated_at       DATETIME           NULL,
    PRIMARY KEY (id),
    CONSTRAINT FK_shelf_rack
        FOREIGN KEY (rack_id) REFERENCES rack (id)
--     constraint FK_shelf_asset_type foreign key (asset_type_id) references asset_type (id)
);