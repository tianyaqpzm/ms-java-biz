-- V1.3__recipe_and_food_schema.sql

-- Insert default Recipe topic for RAG
INSERT INTO knowledge_topic (id, name, icon, description, visible_scope)
VALUES ('topic_recipe_001', '菜谱', 'restaurant', '专业美食菜谱知识库', 'public')
ON CONFLICT (id) DO NOTHING;

-- Create recipe table for business logic (Singular naming with ms_ prefix)
CREATE TABLE IF NOT EXISTS ms_recipe (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(64),
    difficulty VARCHAR(32),
    image_url TEXT,
    ingredients TEXT,
    instructions TEXT,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE ms_recipe IS '菜谱信息表';
COMMENT ON COLUMN ms_recipe.id IS '主键ID';
COMMENT ON COLUMN ms_recipe.name IS '菜品名称';
COMMENT ON COLUMN ms_recipe.category IS '菜品分类';
COMMENT ON COLUMN ms_recipe.difficulty IS '烹饪难度';
COMMENT ON COLUMN ms_recipe.ingredients IS '配料明细';
COMMENT ON COLUMN ms_recipe.instructions IS '烹饪步骤';

-- Create user favorite table
CREATE TABLE IF NOT EXISTS ms_user_favorite (
    id SERIAL PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    recipe_id INTEGER NOT NULL,
    create_time TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, recipe_id)
);

COMMENT ON TABLE ms_user_favorite IS '用户菜谱收藏表';
COMMENT ON COLUMN ms_user_favorite.user_id IS '用户ID';
COMMENT ON COLUMN ms_user_favorite.recipe_id IS '菜谱ID';

-- Mock data for recipe
INSERT INTO ms_recipe (name, category, difficulty, ingredients, instructions)
VALUES 
('四川火锅', '川菜', '中等', '火锅底料, 牛肉, 蔬菜', '1. 煮沸底料 2. 加入食材'),
('广式点心', '粤菜', '困难', '面粉, 虾仁, 猪肉', '1. 包裹食材 2. 蒸熟'),
('东坡肉', '鲁菜', '中等', '五花肉, 酱油, 糖', '1. 炖煮 2. 收汁');
