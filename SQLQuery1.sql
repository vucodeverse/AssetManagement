CREATE TABLE asset_handover (
                                handover_id             INT IDENTITY(1,1) NOT NULL PRIMARY KEY,

                                handover_type           NVARCHAR(40) NOT NULL,

                                allocation_request_id   INT NULL,
                                return_request_id       INT NULL,

                                from_department_id      INT NULL, -- Null nếu xuất thẳng từ kho chưa quy định dept
                                to_department_id        INT NULL, -- Null nếu thu về kho

                                status                  NVARCHAR(40) NOT NULL, -- DRAFT, COMPLETED, CANCELLED

                                created_at              DATETIME2(0) NOT NULL DEFAULT SYSDATETIME(),
                                updated_at              DATETIME2(0) NULL,

                                CONSTRAINT FK_ho_alloc_req FOREIGN KEY (allocation_request_id) REFERENCES allocation_request(request_id),
                                CONSTRAINT FK_ho_ret_req FOREIGN KEY (return_request_id) REFERENCES return_request(request_id),
                                CONSTRAINT FK_ho_from_dept FOREIGN KEY (from_department_id) REFERENCES departments(department_id),
                                CONSTRAINT FK_ho_to_dept FOREIGN KEY (to_department_id) REFERENCES departments(department_id),
);

CREATE TABLE asset_handover_detail (
                                       handover_detail_id      INT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                                       handover_id             INT NOT NULL REFERENCES asset_handover(handover_id),
                                       asset_id                INT NOT NULL REFERENCES asset(asset_id),
                                       qc_report_id            INT NULL REFERENCES qc_report(id),
                                       note                    NVARCHAR(255) NULL
);