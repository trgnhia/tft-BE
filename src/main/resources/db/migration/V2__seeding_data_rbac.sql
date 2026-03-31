insert into roles (code, name, description)
values
    ('ADMIN', 'Administrator', 'Full system access'),
    ('EDITOR', 'Editor', 'Can manage content'),
    ('USER', 'User', 'Basic access');

insert into permissions (code, name, description)
values
    ('USER_READ', 'Read users', 'View user information'),
    ('USER_CREATE', 'Create users', 'Create new users'),
    ('USER_UPDATE', 'Update users', 'Update existing users'),
    ('USER_DELETE', 'Delete users', 'Delete users'),
    ('ROLE_READ', 'Read roles', 'View roles'),
    ('ROLE_UPDATE', 'Update roles', 'Update roles'),
    ('PERMISSION_READ', 'Read permissions', 'View permissions');

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code in (
                                          'USER_READ',
                                          'USER_CREATE',
                                          'USER_UPDATE',
                                          'USER_DELETE',
                                          'ROLE_READ',
                                          'ROLE_UPDATE',
                                          'PERMISSION_READ'
    )
where r.code = 'ADMIN';

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code in (
                                          'USER_READ',
                                          'USER_UPDATE',
                                          'ROLE_READ',
                                          'PERMISSION_READ'
    )
where r.code = 'EDITOR';

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
         join permissions p on p.code in (
    'USER_READ'
    )
where r.code = 'USER';

insert into users (username, email, password_hash, enabled, role_id)
select
    'admin',
    'admin@example.com',
    '$2a$10$7QJQ0KqV6tT9mYJXWmKk4eY8z7mK0lR0n2M4P6m9w0c8mK1gYx9aS',
    true,
    r.id
from roles r
where r.code = 'ADMIN';