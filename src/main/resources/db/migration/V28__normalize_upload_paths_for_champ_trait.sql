-- Normalize legacy managed asset paths to production-safe relative paths.
-- Target format in DB: /uploads/<folder>/<file>

UPDATE champs
SET image_url = REPLACE(image_url, '/api/v1', '')
WHERE image_url LIKE '/api/v1/uploads/%';

UPDATE traits
SET icon_url = REPLACE(icon_url, '/api/v1', '')
WHERE icon_url LIKE '/api/v1/uploads/%';

-- Handle rare values without leading slash (api/v1/uploads/...)
UPDATE champs
SET image_url = '/' || image_url
WHERE image_url LIKE 'uploads/%';

UPDATE traits
SET icon_url = '/' || icon_url
WHERE icon_url LIKE 'uploads/%';

UPDATE champs
SET image_url = REPLACE(image_url, 'api/v1/uploads/', '/uploads/')
WHERE image_url LIKE 'api/v1/uploads/%';

UPDATE traits
SET icon_url = REPLACE(icon_url, 'api/v1/uploads/', '/uploads/')
WHERE icon_url LIKE 'api/v1/uploads/%';

-- Handle absolute URLs persisted in DB: http(s)://host/api/v1/uploads/...
UPDATE champs
SET image_url = regexp_replace(image_url, '^https?://[^/]+/api/v1/uploads/', '/uploads/')
WHERE image_url ~* '^https?://[^/]+/api/v1/uploads/';

UPDATE traits
SET icon_url = regexp_replace(icon_url, '^https?://[^/]+/api/v1/uploads/', '/uploads/')
WHERE icon_url ~* '^https?://[^/]+/api/v1/uploads/';
