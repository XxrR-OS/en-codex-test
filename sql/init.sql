-- =====================================================
-- EnglishLearningPlatform 数据库初始化脚本
-- MySQL 8.0+
-- =====================================================

CREATE DATABASE IF NOT EXISTS english_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE english_platform;
SET NAMES utf8mb4;

-- =====================================================
-- 1. 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `user` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username`    VARCHAR(50)  NOT NULL                COMMENT '用户名',
  `password`    VARCHAR(255) NOT NULL                COMMENT '密码(BCrypt加密)',
  `nickname`    VARCHAR(50)  DEFAULT NULL            COMMENT '昵称',
  `email`       VARCHAR(100) DEFAULT NULL            COMMENT '邮箱',
  `avatar`      VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
  `level`       TINYINT      NOT NULL DEFAULT 1      COMMENT '英语等级 1-初级 2-中级 3-高级',
  `total_score` INT          NOT NULL DEFAULT 0      COMMENT '总积分',
  `status`      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 0-禁用 1-正常',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 2. 单词表
-- =====================================================
CREATE TABLE IF NOT EXISTS `word` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT,
  `word`          VARCHAR(100) NOT NULL             COMMENT '英文单词',
  `phonetic`      VARCHAR(100) DEFAULT NULL         COMMENT '音标',
  `translation`   VARCHAR(500) NOT NULL             COMMENT '中文释义',
  `example`       TEXT         DEFAULT NULL         COMMENT '例句',
  `example_trans` TEXT         DEFAULT NULL         COMMENT '例句翻译',
  `level`         TINYINT      NOT NULL DEFAULT 1   COMMENT '难度 1-初 2-中 3-高',
  `category`      VARCHAR(50)  DEFAULT NULL         COMMENT '分类(CET4/CET6/IELTS等)',
  `audio_url`     VARCHAR(500) DEFAULT NULL         COMMENT '发音音频URL',
  `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_level` (`level`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单词表';

-- =====================================================
-- 3. 知识点表
-- =====================================================
CREATE TABLE IF NOT EXISTS `knowledge_point` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL              COMMENT '知识点名称',
  `category`    VARCHAR(50)  NOT NULL              COMMENT '分类(语法/词汇/阅读/写作/听力)',
  `description` TEXT         DEFAULT NULL          COMMENT '知识点描述',
  `parent_id`   BIGINT       DEFAULT NULL          COMMENT '父知识点ID',
  PRIMARY KEY (`id`),
  INDEX `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识点表';

-- =====================================================
-- 4. 题目表
-- =====================================================
CREATE TABLE IF NOT EXISTS `question` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `title`            TEXT         NOT NULL             COMMENT '题目内容',
  `type`             TINYINT      NOT NULL             COMMENT '题型 1-单选 2-多选 3-填空 4-翻译',
  `difficulty`       TINYINT      NOT NULL DEFAULT 2   COMMENT '难度 1-易 2-中 3-难',
  `knowledge_id`     BIGINT       DEFAULT NULL         COMMENT '关联知识点ID',
  `answer`           TEXT         NOT NULL             COMMENT '正确答案',
  `analysis`         TEXT         DEFAULT NULL         COMMENT '题目解析',
  `score`            INT          NOT NULL DEFAULT 10  COMMENT '分值',
  `source`           VARCHAR(50)  DEFAULT NULL         COMMENT '来源(AI/手动)',
  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_difficulty` (`difficulty`),
  INDEX `idx_knowledge` (`knowledge_id`),
  INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- =====================================================
-- 5. 题目选项表（单选/多选）
-- =====================================================
CREATE TABLE IF NOT EXISTS `question_option` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `question_id` BIGINT       NOT NULL             COMMENT '题目ID',
  `option_key`  CHAR(1)      NOT NULL             COMMENT '选项标识 A/B/C/D',
  `option_value` TEXT        NOT NULL             COMMENT '选项内容',
  PRIMARY KEY (`id`),
  INDEX `idx_question` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';

-- =====================================================
-- 6. 用户单词学习记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_word_record` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`       BIGINT   NOT NULL               COMMENT '用户ID',
  `word_id`       BIGINT   NOT NULL               COMMENT '单词ID',
  `study_count`   INT      NOT NULL DEFAULT 0     COMMENT '学习次数',
  `correct_count` INT      NOT NULL DEFAULT 0     COMMENT '正确次数',
  `wrong_count`   INT      NOT NULL DEFAULT 0     COMMENT '错误次数',
  `mastered`      TINYINT  NOT NULL DEFAULT 0     COMMENT '是否掌握 0-否 1-是',
  `next_review`   DATETIME DEFAULT NULL           COMMENT '下次复习时间(艾宾浩斯)',
  `last_study`    DATETIME DEFAULT NULL           COMMENT '上次学习时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_word` (`user_id`, `word_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户单词学习记录';

-- =====================================================
-- 7. 用户答题记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_question_record` (
  `id`           BIGINT    NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT    NOT NULL               COMMENT '用户ID',
  `question_id`  BIGINT    NOT NULL               COMMENT '题目ID',
  `knowledge_id` BIGINT    DEFAULT NULL           COMMENT '知识点ID',
  `user_answer`  TEXT      DEFAULT NULL           COMMENT '用户答案',
  `is_correct`   TINYINT   NOT NULL DEFAULT 0     COMMENT '是否正确 0-错 1-对',
  `score_got`    INT       NOT NULL DEFAULT 0     COMMENT '得分',
  `time_spent`   INT       DEFAULT NULL           COMMENT '用时(秒)',
  `create_time`  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_question` (`question_id`),
  INDEX `idx_correct` (`user_id`, `is_correct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户答题记录';

-- =====================================================
-- 8. 用户知识点薄弱统计
-- =====================================================
CREATE TABLE IF NOT EXISTS `user_knowledge_stat` (
  `id`             BIGINT    NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT    NOT NULL               COMMENT '用户ID',
  `knowledge_id`   BIGINT    NOT NULL               COMMENT '知识点ID',
  `total_count`    INT       NOT NULL DEFAULT 0     COMMENT '答题总数',
  `correct_count`  INT       NOT NULL DEFAULT 0     COMMENT '正确数',
  `correct_rate`   DECIMAL(5,2) NOT NULL DEFAULT 0  COMMENT '正确率',
  `weak_score`     INT       NOT NULL DEFAULT 50    COMMENT '薄弱评分(越低越薄弱)',
  `update_time`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_knowledge` (`user_id`, `knowledge_id`),
  INDEX `idx_user_weak` (`user_id`, `weak_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户知识点薄弱统计';

-- =====================================================
-- 9. 发音评测记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `pronunciation_record` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT,
  `user_id`         BIGINT        NOT NULL               COMMENT '用户ID',
  `word_text`       VARCHAR(500)  NOT NULL               COMMENT '评测文本',
  `audio_url`       VARCHAR(500)  DEFAULT NULL           COMMENT '音频文件URL',
  `total_score`     DECIMAL(5,2)  DEFAULT NULL           COMMENT '总分',
  `accuracy_score`  DECIMAL(5,2)  DEFAULT NULL           COMMENT '准确度分',
  `fluency_score`   DECIMAL(5,2)  DEFAULT NULL           COMMENT '流利度分',
  `word_scores`     JSON          DEFAULT NULL           COMMENT '单词级别评分JSON',
  `feedback`        TEXT          DEFAULT NULL           COMMENT 'AI反馈建议',
  `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发音评测记录';

-- =====================================================
-- 10. 作文批改记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `essay` (
  `id`           BIGINT    NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT    NOT NULL               COMMENT '用户ID',
  `topic`        VARCHAR(500) NOT NULL            COMMENT '作文题目',
  `content`      TEXT      NOT NULL               COMMENT '用户作文内容',
  `total_score`  DECIMAL(5,2) DEFAULT NULL        COMMENT 'AI批改总分(满分100)',
  `grammar_score` DECIMAL(5,2) DEFAULT NULL       COMMENT '语法分',
  `content_score` DECIMAL(5,2) DEFAULT NULL       COMMENT '内容分',
  `structure_score` DECIMAL(5,2) DEFAULT NULL     COMMENT '结构分',
  `vocabulary_score` DECIMAL(5,2) DEFAULT NULL    COMMENT '词汇分',
  `feedback`     TEXT      DEFAULT NULL           COMMENT 'AI批改详细反馈',
  `correction`   TEXT      DEFAULT NULL           COMMENT 'AI修改建议版本',
  `create_time`  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='英语作文批改记录';

-- =====================================================
-- 11. 学习计划/打卡记录
-- =====================================================
CREATE TABLE IF NOT EXISTS `study_checkin` (
  `id`          BIGINT   NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT   NOT NULL               COMMENT '用户ID',
  `checkin_date` DATE    NOT NULL               COMMENT '打卡日期',
  `word_count`  INT      NOT NULL DEFAULT 0     COMMENT '当天学习单词数',
  `question_count` INT   NOT NULL DEFAULT 0     COMMENT '当天答题数',
  `score_got`   INT      NOT NULL DEFAULT 0     COMMENT '当天得分',
  `study_minutes` INT    NOT NULL DEFAULT 0     COMMENT '学习时长(分钟)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习打卡记录';

-- =====================================================
-- 初始化知识点数据
-- =====================================================
INSERT INTO `knowledge_point` (`name`, `category`, `description`) VALUES
('词汇基础',       '词汇', '基础词汇学习'),
('语法-时态',      '语法', '英语时态用法'),
('语法-从句',      '语法', '英语从句结构'),
('语法-虚拟语气',  '语法', '虚拟语气用法'),
('阅读理解',       '阅读', '阅读理解技巧'),
('完形填空',       '阅读', '完形填空解题'),
('写作技巧',       '写作', '英语写作方法'),
('听力理解',       '听力', '英语听力训练'),
('口语表达',       '口语', '英语口语练习'),
('翻译技巧',       '翻译', '英汉互译技巧');

-- =====================================================
-- 初始化示例单词
-- =====================================================
INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `example_trans`, `level`, `category`) VALUES
('abandon',   '/əˈbændən/',  '抛弃；放弃',     'He abandoned his car on the highway.',               '他把车遗弃在高速公路上。',      2, 'CET4'),
('abundant',  '/əˈbʌndənt/', '丰富的；大量的', 'The region has abundant natural resources.',          '该地区拥有丰富的自然资源。',    2, 'CET4'),
('acknowledge','/əkˈnɒlɪdʒ/','承认；致谢',     'She acknowledged her mistake.',                       '她承认了自己的错误。',          2, 'CET4'),
('ambiguous', '/æmˈbɪɡjuəs/','模糊的；不明确的','The contract contained ambiguous language.',          '合同中含有模糊的措辞。',        3, 'CET6'),
('articulate','/ɑːˈtɪkjuleɪt/','表达清晰的',   'She is an articulate speaker.',                       '她是一个口才出众的演讲者。',    3, 'CET6'),
('brief',     '/briːf/',     '简短的；简要说明','Please give a brief introduction.',                   '请做一个简短的介绍。',          1, 'CET4'),
('cautious',  '/ˈkɔːʃəs/',  '谨慎的；小心的', 'Be cautious when crossing the road.',                  '过马路时要小心。',              2, 'CET4'),
('crucial',   '/ˈkruːʃəl/', '至关重要的',      'Teamwork is crucial to success.',                     '团队合作对成功至关重要。',      2, 'CET6'),
('demonstrate','/ˈdemənstreɪt/','证明；示范',  'The experiment demonstrates the theory.',              '实验证明了这一理论。',          2, 'CET4'),
('elaborate', '/ɪˈlæbərət/', '精心制作的；详尽','She gave an elaborate explanation.',                  '她给出了详尽的解释。',          3, 'CET6');

INSERT IGNORE INTO `word` (`id`, `word`, `phonetic`, `translation`, `example`, `example_trans`, `level`, `category`) VALUES
(11, 'adapt', '/əˈdæpt/', '适应；改编', 'We must adapt to the new environment.', '我们必须适应新环境。', 2, 'CET4'),
(12, 'analysis', '/əˈnæləsɪs/', '分析', 'The report provides a detailed analysis of the problem.', '这份报告对问题进行了详细分析。', 2, 'CET4'),
(13, 'approach', '/əˈprəʊtʃ/', '方法；接近', 'We need a new approach to language learning.', '我们需要一种新的语言学习方法。', 2, 'CET4'),
(14, 'consume', '/kənˈsjuːm/', '消耗；消费', 'Reading news in English consumes less time now.', '现在阅读英语新闻花费的时间更少了。', 2, 'CET6'),
(15, 'decline', '/dɪˈklaɪn/', '下降；拒绝', 'The number of errors began to decline.', '错误数量开始下降。', 2, 'CET4'),
(16, 'efficient', '/ɪˈfɪʃənt/', '高效的', 'A good review plan makes vocabulary learning efficient.', '好的复习计划会让词汇学习更高效。', 2, 'CET4'),
(17, 'factor', '/ˈfæktə(r)/', '因素', 'Interest is an important factor in study.', '兴趣是学习中的一个重要因素。', 1, 'CET4'),
(18, 'illustrate', '/ˈɪləstreɪt/', '说明；阐明', 'The teacher used an example to illustrate the rule.', '老师用一个例子来说明这个规则。', 2, 'CET6'),
(19, 'maintain', '/meɪnˈteɪn/', '保持；维持', 'It is hard to maintain concentration for a long time.', '长时间保持专注很难。', 2, 'CET4'),
(20, 'obvious', '/ˈɒbviəs/', '明显的', 'The answer is obvious from the context.', '从上下文中可以明显看出答案。', 1, 'CET4'),
(21, 'participate', '/pɑːˈtɪsɪpeɪt/', '参加', 'Students are encouraged to participate in discussion.', '鼓励学生参与讨论。', 2, 'CET4'),
(22, 'principle', '/ˈprɪnsəpl/', '原则；原理', 'Practice and feedback are basic learning principles.', '练习和反馈是基本的学习原则。', 2, 'CET6'),
(23, 'significant', '/sɪɡˈnɪfɪkənt/', '重要的；显著的', 'There has been a significant improvement in her writing.', '她的写作有了显著提高。', 2, 'CET4'),
(24, 'strategy', '/ˈstrætədʒi/', '策略', 'You need a clear strategy for exam preparation.', '你需要一个清晰的备考策略。', 2, 'CET6'),
(25, 'tend', '/tend/', '倾向于；往往会', 'Beginners tend to make tense mistakes.', '初学者往往会犯时态错误。', 1, 'CET4');

-- 补充各类单词本示例词汇，保证进入每个预置单词本后都有可学习内容
INSERT IGNORE INTO `word` (`word`, `phonetic`, `translation`, `example`, `example_trans`, `level`, `category`) VALUES
('comprehensive', '/ˌkɒmprɪˈhensɪv/', '全面的；综合的', 'The course offers comprehensive training for learners.', '这门课程为学习者提供全面训练。', 3, 'CET6'),
('contemporary', '/kənˈtemprəri/', '当代的；同时期的', 'Contemporary students often use online learning tools.', '当代学生经常使用在线学习工具。', 3, 'CET6'),
('inevitable', '/ɪnˈevɪtəbl/', '不可避免的', 'Mistakes are inevitable in the learning process.', '错误在学习过程中是不可避免的。', 3, 'CET6'),
('perspective', '/pəˈspektɪv/', '观点；视角', 'Reading widely helps students gain a new perspective.', '广泛阅读有助于学生获得新的视角。', 3, 'CET6'),
('substantial', '/səbˈstænʃl/', '大量的；实质性的', 'Regular practice leads to substantial improvement.', '规律练习会带来实质性提升。', 3, 'CET6'),
('priority', '/praɪˈɒrəti/', '优先事项', 'Vocabulary review should be a daily priority.', '词汇复习应当成为每日优先事项。', 2, 'CET6'),
('undertake', '/ˌʌndəˈteɪk/', '承担；从事', 'The team will undertake a new research project.', '该团队将承担一个新的研究项目。', 3, 'CET6'),
('phenomenon', '/fəˈnɒmɪnən/', '现象', 'Online education is now a common phenomenon.', '在线教育现在是一种常见现象。', 3, 'CET6'),
('adjective', '/ˈædʒɪktɪv/', '形容词', 'An adjective is used to describe a noun.', '形容词用于描述名词。', 1, 'TEM4'),
('clause', '/klɔːz/', '从句；分句', 'The sentence contains a relative clause.', '这个句子包含一个关系从句。', 2, 'TEM4'),
('coherence', '/kəʊˈhɪərəns/', '连贯性', 'Coherence is important in essay writing.', '连贯性在作文写作中很重要。', 2, 'TEM4'),
('dictation', '/dɪkˈteɪʃn/', '听写', 'Dictation can improve listening and spelling.', '听写可以提高听力和拼写能力。', 1, 'TEM4'),
('idiom', '/ˈɪdiəm/', '习语；成语', 'Learning idioms makes expression more natural.', '学习习语能让表达更自然。', 2, 'TEM4'),
('intonation', '/ˌɪntəˈneɪʃn/', '语调', 'Intonation affects the meaning of spoken English.', '语调会影响英语口语的含义。', 2, 'TEM4'),
('morphology', '/mɔːˈfɒlədʒi/', '形态学；词法', 'Morphology studies the structure of words.', '形态学研究词语的结构。', 3, 'TEM4'),
('syntax', '/ˈsɪntæks/', '句法', 'Syntax explains how words form sentences.', '句法解释词语如何组成句子。', 3, 'TEM4'),
('aesthetic', '/iːsˈθetɪk/', '审美的；美学的', 'The poem has strong aesthetic value.', '这首诗具有较强的审美价值。', 3, 'TEM8'),
('allegory', '/ˈælɪɡəri/', '寓言；讽喻', 'The story can be read as a political allegory.', '这个故事可以被解读为政治讽喻。', 3, 'TEM8'),
('ambiguity', '/ˌæmbɪˈɡjuːəti/', '歧义；模糊', 'Ambiguity may cause misunderstanding in translation.', '歧义可能导致翻译中的误解。', 3, 'TEM8'),
('cohesion', '/kəʊˈhiːʒn/', '衔接；凝聚力', 'Cohesion makes a paragraph easier to follow.', '衔接能让段落更容易理解。', 3, 'TEM8'),
('discourse', '/ˈdɪskɔːs/', '话语；语篇', 'Discourse analysis focuses on language in context.', '语篇分析关注语境中的语言。', 3, 'TEM8'),
('metaphor', '/ˈmetəfə(r)/', '隐喻', 'A metaphor compares two things indirectly.', '隐喻以间接方式比较两个事物。', 3, 'TEM8'),
('pragmatic', '/præɡˈmætɪk/', '语用的；务实的', 'A pragmatic approach solves real communication problems.', '务实的方法能解决实际交流问题。', 3, 'TEM8'),
('rhetoric', '/ˈretərɪk/', '修辞；修辞学', 'Rhetoric can make an argument more persuasive.', '修辞能让论证更有说服力。', 3, 'TEM8'),
('allocate', '/ˈæləkeɪt/', '分配；配置', 'You should allocate enough time for revision.', '你应该为复习分配足够时间。', 2, 'KAOYAN'),
('alternative', '/ɔːlˈtɜːnətɪv/', '可替代的；选择', 'Online learning is an alternative to traditional classes.', '在线学习是传统课堂的一种替代选择。', 2, 'KAOYAN'),
('circumstance', '/ˈsɜːkəmstəns/', '情况；环境', 'Students should adjust plans according to circumstances.', '学生应根据情况调整计划。', 2, 'KAOYAN'),
('consequence', '/ˈkɒnsɪkwəns/', '结果；后果', 'Careless reading may lead to serious consequences.', '粗心阅读可能导致严重后果。', 2, 'KAOYAN'),
('emphasis', '/ˈemfəsɪs/', '强调；重点', 'The teacher placed emphasis on grammar review.', '老师强调了语法复习。', 2, 'KAOYAN'),
('hypothesis', '/haɪˈpɒθəsɪs/', '假设', 'The experiment was designed to test the hypothesis.', '该实验旨在验证这一假设。', 3, 'KAOYAN'),
('interpret', '/ɪnˈtɜːprɪt/', '解释；理解', 'Readers may interpret the passage in different ways.', '读者可能以不同方式理解这篇文章。', 2, 'KAOYAN'),
('policy', '/ˈpɒləsi/', '政策；方针', 'Education policy affects language learning resources.', '教育政策会影响语言学习资源。', 2, 'KAOYAN'),
('accommodation', '/əˌkɒməˈdeɪʃn/', '住宿；适应', 'The university provides accommodation for international students.', '大学为国际学生提供住宿。', 2, 'IELTS'),
('assessment', '/əˈsesmənt/', '评估；评价', 'The assessment includes speaking and writing tasks.', '评估包括口语和写作任务。', 2, 'IELTS'),
('criteria', '/kraɪˈtɪəriə/', '标准；准则', 'The essay is marked according to clear criteria.', '作文按照明确标准评分。', 2, 'IELTS'),
('diversity', '/daɪˈvɜːsəti/', '多样性', 'Cultural diversity is a common IELTS topic.', '文化多样性是雅思常见话题。', 2, 'IELTS'),
('infrastructure', '/ˈɪnfrəstrʌktʃə(r)/', '基础设施', 'Good infrastructure supports urban development.', '良好的基础设施支持城市发展。', 3, 'IELTS'),
('migration', '/maɪˈɡreɪʃn/', '迁移；移民', 'Migration changes the structure of cities.', '迁移会改变城市结构。', 2, 'IELTS'),
('sustainable', '/səˈsteɪnəbl/', '可持续的', 'Sustainable development protects future resources.', '可持续发展保护未来资源。', 3, 'IELTS'),
('urban', '/ˈɜːbən/', '城市的', 'Urban life offers more educational opportunities.', '城市生活提供更多教育机会。', 2, 'IELTS'),
('academic', '/ˌækəˈdemɪk/', '学术的', 'Academic writing requires clear logic.', '学术写作需要清晰逻辑。', 2, 'TOEFL'),
('campus', '/ˈkæmpəs/', '校园', 'The campus has several language learning centers.', '校园里有几个语言学习中心。', 1, 'TOEFL'),
('lecture', '/ˈlektʃə(r)/', '讲座；授课', 'Students took notes during the lecture.', '学生们在讲座期间记笔记。', 1, 'TOEFL'),
('semester', '/sɪˈmestə(r)/', '学期', 'The course lasts one semester.', '这门课程持续一个学期。', 1, 'TOEFL'),
('research', '/rɪˈsɜːtʃ/', '研究', 'Research skills are essential for university study.', '研究能力对大学学习很重要。', 2, 'TOEFL'),
('thesis', '/ˈθiːsɪs/', '论文；论点', 'She is writing a thesis on language learning.', '她正在写一篇关于语言学习的论文。', 2, 'TOEFL'),
('tuition', '/tjuˈɪʃn/', '学费；教学', 'Tuition fees vary among universities.', '不同大学的学费各不相同。', 2, 'TOEFL'),
('undergraduate', '/ˌʌndəˈɡrædʒuət/', '本科生', 'Undergraduate students often take general courses.', '本科生通常会学习通识课程。', 2, 'TOEFL');

-- =====================================================
-- 初始化示例题目
-- =====================================================
INSERT INTO `question` (`title`, `type`, `difficulty`, `knowledge_id`, `answer`, `analysis`) VALUES
('Which of the following words means "to give up"?', 1, 1, 1, 'A', 'abandon means to give up or leave something permanently.'),
('Choose the correct form: She ___ (study) English for three years.', 1, 2, 2, 'B', 'has studied - present perfect tense for actions continuing to the present.'),
('The sentence "If I were rich, I would travel the world." uses ___', 1, 2, 4, 'C', 'This is a subjunctive mood expressing hypothetical situation.'),
('What is the main idea of the passage? (Reading comprehension)', 1, 3, 5, 'B', 'The main idea is found in the topic sentence of the first paragraph.'),
('Translate: "科技改变了我们的生活方式。"', 4, 2, 10, 'Technology has changed our way of life.', 'Standard translation using present perfect tense.');

INSERT IGNORE INTO `question` (`id`, `title`, `type`, `difficulty`, `knowledge_id`, `answer`, `analysis`, `score`, `source`) VALUES
(6, 'Choose the word closest in meaning to "important".', 1, 1, 1, 'C', 'crucial means very important.', 10, 'manual'),
(7, 'Choose the correct form: By the time we arrived, the meeting ___.', 1, 2, 2, 'B', 'Past perfect is used for an action completed before another past action.', 10, 'manual'),
(8, 'Which sentence contains an attributive clause?', 1, 2, 3, 'A', 'A relative clause modifies a noun.', 10, 'manual'),
(9, 'If he ___ more carefully, he would not make so many mistakes.', 1, 2, 4, 'D', 'The subjunctive mood uses the past tense form to express an unreal present condition.', 10, 'manual'),
(10, 'Read the sentence: "Online learning offers flexible schedules for busy students." The key advantage mentioned is ___.', 1, 1, 5, 'B', 'The sentence clearly points to flexible schedules.', 10, 'manual'),
(11, 'Choose the best word to complete the sentence: A good learning ___ can improve efficiency.', 1, 1, 1, 'D', 'strategy best fits the sentence meaning.', 10, 'manual'),
(12, 'Choose the correct form: He usually ___ to school by bike, but today he is walking.', 1, 1, 2, 'A', 'Simple present is used for habitual actions.', 10, 'manual'),
(13, 'Which of the following words can best replace "clear" in the sentence "The answer is clear"?', 1, 1, 1, 'B', 'obvious means easy to notice or understand.', 10, 'manual'),
(14, 'Select the sentence that contains a noun clause.', 1, 2, 3, 'C', 'A noun clause can function as an object in the sentence.', 10, 'manual'),
(15, 'Choose the correct translation for "他往往在考试前感到紧张。"', 1, 2, 10, 'A', 'tend to is used to express a habitual tendency.', 10, 'manual'),
(16, 'Which sentence best shows the use of present perfect tense?', 1, 2, 2, 'D', 'Present perfect connects past actions with the present.', 10, 'manual'),
(17, 'Translate: "坚持练习是提高口语的关键。"', 4, 2, 10, 'Continuous practice is the key to improving spoken English.', 'The sentence focuses on persistence and the key to improvement.', 10, 'manual'),
(18, 'What does the word "adapt" most nearly mean?', 1, 1, 1, 'C', 'adapt means to adjust to new conditions.', 10, 'manual'),
(19, 'Choose the sentence with correct tense usage.', 1, 2, 2, 'B', 'The sentence uses the present perfect correctly with "since".', 10, 'manual'),
(20, 'If I ___ enough time tomorrow, I would revise all the notes again.', 1, 3, 4, 'A', 'In unreal future/present conditions, "had" is often used in basic teaching materials here.', 10, 'manual'),
(21, 'The author mentions "efficient review" mainly to show that ___.', 1, 2, 5, 'D', 'The statement emphasizes the value of a proper review method.', 10, 'manual'),
(22, 'Fill in the blank with the best answer: Students should ___ actively in class discussion.', 1, 1, 1, 'C', 'participate is the correct collocation with "in".', 10, 'manual'),
(23, 'Choose the sentence that uses a relative pronoun correctly.', 1, 2, 3, 'B', 'The clause is introduced properly and modifies the noun.', 10, 'manual'),
(24, 'Translate: "有效的方法能够帮助我们保持学习动力。"', 4, 2, 10, 'Effective methods can help us maintain motivation for learning.', 'maintain motivation is a natural collocation in English.', 10, 'manual'),
(25, 'Which option best completes the sentence: Practice and feedback are two important learning ___.', 1, 2, 1, 'A', 'principles is the best choice semantically and grammatically.', 10, 'manual');

INSERT INTO `question_option` (`question_id`, `option_key`, `option_value`) VALUES
(1, 'A', 'abandon'), (1, 'B', 'abundant'), (1, 'C', 'achieve'), (1, 'D', 'advance'),
(2, 'A', 'studied'), (2, 'B', 'has studied'), (2, 'C', 'is studying'), (2, 'D', 'had studied'),
(3, 'A', 'Simple present'), (3, 'B', 'Past perfect'), (3, 'C', 'Subjunctive mood'), (3, 'D', 'Future tense'),
(4, 'A', 'Technology is harmful'), (4, 'B', 'Technology brings changes'), (4, 'C', 'People dislike technology'), (4, 'D', 'Technology is expensive');

INSERT IGNORE INTO `question_option` (`question_id`, `option_key`, `option_value`) VALUES
(6, 'A', 'brief'), (6, 'B', 'cautious'), (6, 'C', 'crucial'), (6, 'D', 'ambiguous'),
(7, 'A', 'has begun'), (7, 'B', 'had begun'), (7, 'C', 'begins'), (7, 'D', 'will begin'),
(8, 'A', 'The book that you lent me is useful.'), (8, 'B', 'I believe that he is honest.'), (8, 'C', 'When he arrived, we had left.'), (8, 'D', 'Please tell me the truth.'),
(9, 'A', 'listens'), (9, 'B', 'listened'), (9, 'C', 'has listened'), (9, 'D', 'listened'),
(10, 'A', 'online courses are cheaper'), (10, 'B', 'students can study at convenient times'), (10, 'C', 'teachers work less'), (10, 'D', 'students do not need homework'),
(11, 'A', 'factor'), (11, 'B', 'analysis'), (11, 'C', 'principle'), (11, 'D', 'strategy'),
(12, 'A', 'goes'), (12, 'B', 'is going'), (12, 'C', 'went'), (12, 'D', 'has gone'),
(13, 'A', 'brief'), (13, 'B', 'obvious'), (13, 'C', 'careful'), (13, 'D', 'formal'),
(14, 'A', 'He was reading when I came in.'), (14, 'B', 'The man who called you is here.'), (14, 'C', 'I know that she can solve the problem.'), (14, 'D', 'Although it rained, we left early.'),
(15, 'A', 'He tends to feel nervous before exams.'), (15, 'B', 'He is nervously before exams.'), (15, 'C', 'He tends feeling nervous before exams.'), (15, 'D', 'He feel nervous before exams.'),
(16, 'A', 'I finished my homework last night.'), (16, 'B', 'I am finishing my homework now.'), (16, 'C', 'I finish my homework every day.'), (16, 'D', 'I have finished my homework already.'),
(18, 'A', 'to explain clearly'), (18, 'B', 'to refuse politely'), (18, 'C', 'to adjust to a new situation'), (18, 'D', 'to repeat something'),
(19, 'A', 'She has went to the library since noon.'), (19, 'B', 'She has studied English since primary school.'), (19, 'C', 'She study English for two hours yesterday.'), (19, 'D', 'She is studied English now.'),
(20, 'A', 'had'), (20, 'B', 'have'), (20, 'C', 'am having'), (20, 'D', 'will have'),
(21, 'A', 'students should stop taking notes'), (21, 'B', 'difficult words should be ignored'), (21, 'C', 'review is unnecessary after class'), (21, 'D', 'a proper method can improve learning results'),
(22, 'A', 'consume'), (22, 'B', 'maintain'), (22, 'C', 'participate'), (22, 'D', 'decline'),
(23, 'A', 'This is the reason because I was late.'), (23, 'B', 'The student who won the prize is my friend.'), (23, 'C', 'The book what I bought is expensive.'), (23, 'D', 'She is the girl which sings well.'),
(25, 'A', 'principles'), (25, 'B', 'strategies'), (25, 'C', 'examples'), (25, 'D', 'mistakes');

-- 初始化管理员账号 (密码: admin123, BCrypt加密)
INSERT INTO `user` (`username`, `password`, `nickname`, `level`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '管理员', 3, 1),
('test',  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '测试用户', 1, 1);
