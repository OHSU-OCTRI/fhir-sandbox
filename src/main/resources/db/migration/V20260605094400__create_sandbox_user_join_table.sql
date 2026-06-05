DROP TABLE IF EXISTS `sandbox_sharing`;
CREATE TABLE `sandbox_sharing` (
    `sandbox` bigint NOT NULL,
    `user` bigint NOT NULL,
    PRIMARY KEY (`sandbox`, `user`),
    CONSTRAINT sandbox_sharing_sandbox_fk FOREIGN KEY (`sandbox`) REFERENCES `sandbox` (`id`) ON DELETE CASCADE,
    CONSTRAINT sandbox_sharing_user_fk FOREIGN KEY (`user`) REFERENCES `user` (`id`) ON DELETE CASCADE
);