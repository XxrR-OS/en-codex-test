USE english_platform;
SET NAMES utf8mb4;

-- 为预置单词本补充词汇；可在已有数据库中重复执行，不会重复插入同一分类下的同一单词。
INSERT INTO `word` (`word`, `phonetic`, `translation`, `example`, `example_trans`, `level`, `category`)
SELECT v.word, v.phonetic, v.translation, v.example, v.example_trans, v.level, v.category
FROM (
  SELECT 'comprehensive' word, '/ˌkɒmprɪˈhensɪv/' phonetic, '全面的；综合的' translation, 'The course offers comprehensive training for learners.' example, '这门课程为学习者提供全面训练。' example_trans, 3 level, 'CET6' category
  UNION ALL SELECT 'contemporary', '/kənˈtemprəri/', '当代的；同时期的', 'Contemporary students often use online learning tools.', '当代学生经常使用在线学习工具。', 3, 'CET6'
  UNION ALL SELECT 'inevitable', '/ɪnˈevɪtəbl/', '不可避免的', 'Mistakes are inevitable in the learning process.', '错误在学习过程中是不可避免的。', 3, 'CET6'
  UNION ALL SELECT 'perspective', '/pəˈspektɪv/', '观点；视角', 'Reading widely helps students gain a new perspective.', '广泛阅读有助于学生获得新的视角。', 3, 'CET6'
  UNION ALL SELECT 'substantial', '/səbˈstænʃl/', '大量的；实质性的', 'Regular practice leads to substantial improvement.', '规律练习会带来实质性提升。', 3, 'CET6'
  UNION ALL SELECT 'priority', '/praɪˈɒrəti/', '优先事项', 'Vocabulary review should be a daily priority.', '词汇复习应当成为每日优先事项。', 2, 'CET6'
  UNION ALL SELECT 'undertake', '/ˌʌndəˈteɪk/', '承担；从事', 'The team will undertake a new research project.', '该团队将承担一个新的研究项目。', 3, 'CET6'
  UNION ALL SELECT 'phenomenon', '/fəˈnɒmɪnən/', '现象', 'Online education is now a common phenomenon.', '在线教育现在是一种常见现象。', 3, 'CET6'
  UNION ALL SELECT 'adjective', '/ˈædʒɪktɪv/', '形容词', 'An adjective is used to describe a noun.', '形容词用于描述名词。', 1, 'TEM4'
  UNION ALL SELECT 'clause', '/klɔːz/', '从句；分句', 'The sentence contains a relative clause.', '这个句子包含一个关系从句。', 2, 'TEM4'
  UNION ALL SELECT 'coherence', '/kəʊˈhɪərəns/', '连贯性', 'Coherence is important in essay writing.', '连贯性在作文写作中很重要。', 2, 'TEM4'
  UNION ALL SELECT 'dictation', '/dɪkˈteɪʃn/', '听写', 'Dictation can improve listening and spelling.', '听写可以提高听力和拼写能力。', 1, 'TEM4'
  UNION ALL SELECT 'idiom', '/ˈɪdiəm/', '习语；成语', 'Learning idioms makes expression more natural.', '学习习语能让表达更自然。', 2, 'TEM4'
  UNION ALL SELECT 'intonation', '/ˌɪntəˈneɪʃn/', '语调', 'Intonation affects the meaning of spoken English.', '语调会影响英语口语的含义。', 2, 'TEM4'
  UNION ALL SELECT 'morphology', '/mɔːˈfɒlədʒi/', '形态学；词法', 'Morphology studies the structure of words.', '形态学研究词语的结构。', 3, 'TEM4'
  UNION ALL SELECT 'syntax', '/ˈsɪntæks/', '句法', 'Syntax explains how words form sentences.', '句法解释词语如何组成句子。', 3, 'TEM4'
  UNION ALL SELECT 'aesthetic', '/iːsˈθetɪk/', '审美的；美学的', 'The poem has strong aesthetic value.', '这首诗具有较强的审美价值。', 3, 'TEM8'
  UNION ALL SELECT 'allegory', '/ˈælɪɡəri/', '寓言；讽喻', 'The story can be read as a political allegory.', '这个故事可以被解读为政治讽喻。', 3, 'TEM8'
  UNION ALL SELECT 'ambiguity', '/ˌæmbɪˈɡjuːəti/', '歧义；模糊', 'Ambiguity may cause misunderstanding in translation.', '歧义可能导致翻译中的误解。', 3, 'TEM8'
  UNION ALL SELECT 'cohesion', '/kəʊˈhiːʒn/', '衔接；凝聚力', 'Cohesion makes a paragraph easier to follow.', '衔接能让段落更容易理解。', 3, 'TEM8'
  UNION ALL SELECT 'discourse', '/ˈdɪskɔːs/', '话语；语篇', 'Discourse analysis focuses on language in context.', '语篇分析关注语境中的语言。', 3, 'TEM8'
  UNION ALL SELECT 'metaphor', '/ˈmetəfə(r)/', '隐喻', 'A metaphor compares two things indirectly.', '隐喻以间接方式比较两个事物。', 3, 'TEM8'
  UNION ALL SELECT 'pragmatic', '/præɡˈmætɪk/', '语用的；务实的', 'A pragmatic approach solves real communication problems.', '务实的方法能解决实际交流问题。', 3, 'TEM8'
  UNION ALL SELECT 'rhetoric', '/ˈretərɪk/', '修辞；修辞学', 'Rhetoric can make an argument more persuasive.', '修辞能让论证更有说服力。', 3, 'TEM8'
  UNION ALL SELECT 'allocate', '/ˈæləkeɪt/', '分配；配置', 'You should allocate enough time for revision.', '你应该为复习分配足够时间。', 2, 'KAOYAN'
  UNION ALL SELECT 'alternative', '/ɔːlˈtɜːnətɪv/', '可替代的；选择', 'Online learning is an alternative to traditional classes.', '在线学习是传统课堂的一种替代选择。', 2, 'KAOYAN'
  UNION ALL SELECT 'circumstance', '/ˈsɜːkəmstəns/', '情况；环境', 'Students should adjust plans according to circumstances.', '学生应根据情况调整计划。', 2, 'KAOYAN'
  UNION ALL SELECT 'consequence', '/ˈkɒnsɪkwəns/', '结果；后果', 'Careless reading may lead to serious consequences.', '粗心阅读可能导致严重后果。', 2, 'KAOYAN'
  UNION ALL SELECT 'emphasis', '/ˈemfəsɪs/', '强调；重点', 'The teacher placed emphasis on grammar review.', '老师强调了语法复习。', 2, 'KAOYAN'
  UNION ALL SELECT 'hypothesis', '/haɪˈpɒθəsɪs/', '假设', 'The experiment was designed to test the hypothesis.', '该实验旨在验证这一假设。', 3, 'KAOYAN'
  UNION ALL SELECT 'interpret', '/ɪnˈtɜːprɪt/', '解释；理解', 'Readers may interpret the passage in different ways.', '读者可能以不同方式理解这篇文章。', 2, 'KAOYAN'
  UNION ALL SELECT 'policy', '/ˈpɒləsi/', '政策；方针', 'Education policy affects language learning resources.', '教育政策会影响语言学习资源。', 2, 'KAOYAN'
  UNION ALL SELECT 'accommodation', '/əˌkɒməˈdeɪʃn/', '住宿；适应', 'The university provides accommodation for international students.', '大学为国际学生提供住宿。', 2, 'IELTS'
  UNION ALL SELECT 'assessment', '/əˈsesmənt/', '评估；评价', 'The assessment includes speaking and writing tasks.', '评估包括口语和写作任务。', 2, 'IELTS'
  UNION ALL SELECT 'criteria', '/kraɪˈtɪəriə/', '标准；准则', 'The essay is marked according to clear criteria.', '作文按照明确标准评分。', 2, 'IELTS'
  UNION ALL SELECT 'diversity', '/daɪˈvɜːsəti/', '多样性', 'Cultural diversity is a common IELTS topic.', '文化多样性是雅思常见话题。', 2, 'IELTS'
  UNION ALL SELECT 'infrastructure', '/ˈɪnfrəstrʌktʃə(r)/', '基础设施', 'Good infrastructure supports urban development.', '良好的基础设施支持城市发展。', 3, 'IELTS'
  UNION ALL SELECT 'migration', '/maɪˈɡreɪʃn/', '迁移；移民', 'Migration changes the structure of cities.', '迁移会改变城市结构。', 2, 'IELTS'
  UNION ALL SELECT 'sustainable', '/səˈsteɪnəbl/', '可持续的', 'Sustainable development protects future resources.', '可持续发展保护未来资源。', 3, 'IELTS'
  UNION ALL SELECT 'urban', '/ˈɜːbən/', '城市的', 'Urban life offers more educational opportunities.', '城市生活提供更多教育机会。', 2, 'IELTS'
  UNION ALL SELECT 'academic', '/ˌækəˈdemɪk/', '学术的', 'Academic writing requires clear logic.', '学术写作需要清晰逻辑。', 2, 'TOEFL'
  UNION ALL SELECT 'campus', '/ˈkæmpəs/', '校园', 'The campus has several language learning centers.', '校园里有几个语言学习中心。', 1, 'TOEFL'
  UNION ALL SELECT 'lecture', '/ˈlektʃə(r)/', '讲座；授课', 'Students took notes during the lecture.', '学生们在讲座期间记笔记。', 1, 'TOEFL'
  UNION ALL SELECT 'semester', '/sɪˈmestə(r)/', '学期', 'The course lasts one semester.', '这门课程持续一个学期。', 1, 'TOEFL'
  UNION ALL SELECT 'research', '/rɪˈsɜːtʃ/', '研究', 'Research skills are essential for university study.', '研究能力对大学学习很重要。', 2, 'TOEFL'
  UNION ALL SELECT 'thesis', '/ˈθiːsɪs/', '论文；论点', 'She is writing a thesis on language learning.', '她正在写一篇关于语言学习的论文。', 2, 'TOEFL'
  UNION ALL SELECT 'tuition', '/tjuˈɪʃn/', '学费；教学', 'Tuition fees vary among universities.', '不同大学的学费各不相同。', 2, 'TOEFL'
  UNION ALL SELECT 'undergraduate', '/ˌʌndəˈɡrædʒuət/', '本科生', 'Undergraduate students often take general courses.', '本科生通常会学习通识课程。', 2, 'TOEFL'
) AS v
WHERE NOT EXISTS (
  SELECT 1
  FROM `word` w
  WHERE w.`word` = v.word AND w.`category` = v.category
);
