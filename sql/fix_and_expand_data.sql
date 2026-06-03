USE english_platform;
SET NAMES utf8mb4;

INSERT INTO `word` (`id`, `word`, `phonetic`, `translation`, `example`, `example_trans`, `level`, `category`)
VALUES
(11, 'adapt', '/əˈdæpt/', '适应；改编', 'We must adapt to the new environment.', '我们必须适应新环境。', 2, 'CET4'),
(12, 'analysis', '/əˈnæləsɪs/', '分析', 'The report provides a detailed analysis of the problem.', '这份报告对问题进行了详细分析。', 2, 'CET4'),
(13, 'approach', '/əˈprəʊtʃ/', '方法；接近', 'We need a new approach to language learning.', '我们需要一种新的语言学习方法。', 2, 'CET4'),
(14, 'consume', '/kənˈsjuːm/', '消耗；消费', 'Online videos may consume too much study time.', '在线视频可能会占用过多学习时间。', 2, 'CET6'),
(15, 'decline', '/dɪˈklaɪn/', '下降；拒绝', 'The number of mistakes began to decline after review.', '复习之后，错误数量开始下降。', 2, 'CET4'),
(16, 'efficient', '/ɪˈfɪʃənt/', '高效的', 'A clear plan makes vocabulary learning efficient.', '清晰的计划会让词汇学习更高效。', 2, 'CET4'),
(17, 'factor', '/ˈfæktə(r)/', '因素', 'Interest is an important factor in language learning.', '兴趣是语言学习中的一个重要因素。', 1, 'CET4'),
(18, 'illustrate', '/ˈɪləstreɪt/', '说明；阐明', 'The teacher used an example to illustrate the rule.', '老师用一个例子来说明这个规则。', 2, 'CET6'),
(19, 'maintain', '/meɪnˈteɪn/', '保持；维持', 'It is hard to maintain concentration for a long time.', '长时间保持专注很难。', 2, 'CET4'),
(20, 'obvious', '/ˈɒbviəs/', '明显的', 'The answer is obvious from the context.', '从上下文中可以明显看出答案。', 1, 'CET4'),
(21, 'participate', '/pɑːˈtɪsɪpeɪt/', '参加', 'Students are encouraged to participate in discussion.', '鼓励学生参与讨论。', 2, 'CET4'),
(22, 'principle', '/ˈprɪnsəpl/', '原则；原理', 'Practice and feedback are basic learning principles.', '练习和反馈是基本的学习原则。', 2, 'CET6'),
(23, 'significant', '/sɪɡˈnɪfɪkənt/', '重要的；显著的', 'There has been a significant improvement in her writing.', '她的写作有了显著提高。', 2, 'CET4'),
(24, 'strategy', '/ˈstrætədʒi/', '策略', 'You need a clear strategy for exam preparation.', '你需要一个清晰的备考策略。', 2, 'CET6'),
(25, 'tend', '/tend/', '倾向于；往往会', 'Beginners tend to make tense mistakes.', '初学者往往会犯时态错误。', 1, 'CET4')
ON DUPLICATE KEY UPDATE
`word` = VALUES(`word`),
`phonetic` = VALUES(`phonetic`),
`translation` = VALUES(`translation`),
`example` = VALUES(`example`),
`example_trans` = VALUES(`example_trans`),
`level` = VALUES(`level`),
`category` = VALUES(`category`);

INSERT INTO `question` (`id`, `title`, `type`, `difficulty`, `knowledge_id`, `answer`, `analysis`, `score`, `source`)
VALUES
(15, 'Choose the correct translation for "他往往在考试前感到紧张。"', 1, 2, 10, 'A', 'tend to is used to express a habitual tendency.', 10, 'manual'),
(17, 'Translate: "坚持练习是提高口语的关键。"', 4, 2, 10, 'Continuous practice is the key to improving spoken English.', 'The sentence focuses on persistence and the key to improvement.', 10, 'manual'),
(24, 'Translate: "有效的方法能够帮助我们保持学习动力。"', 4, 2, 10, 'Effective methods can help us maintain motivation for learning.', 'maintain motivation is a natural collocation in English.', 10, 'manual'),
(26, 'Choose the best meaning of the word "maintain" in the sentence "It is hard to maintain concentration."', 1, 1, 1, 'B', 'maintain means to keep something at the same level or state.', 10, 'manual'),
(27, 'The phrase "significant progress" is closest in meaning to ___.', 1, 1, 1, 'C', 'significant means important or notable.', 10, 'manual'),
(28, 'Choose the best word to complete the sentence: Good habits can ___ learning efficiency.', 1, 1, 1, 'B', 'improve is the most suitable verb here.', 10, 'manual'),
(29, 'What does "consume" mean in the sentence "Online videos may consume too much study time"?', 1, 1, 1, 'B', 'consume means use up.', 10, 'manual'),
(30, 'I ___ English for five years, so I can read simple novels now.', 1, 2, 2, 'C', 'Present perfect is used for an action that started in the past and continues to the present.', 10, 'manual'),
(31, 'When the teacher entered the classroom, the students ___ quietly.', 1, 2, 2, 'D', 'Past continuous shows an action in progress at a particular time in the past.', 10, 'manual'),
(32, 'Choose the sentence with a correct relative clause.', 1, 2, 3, 'B', 'that correctly introduces the clause modifying "book".', 10, 'manual'),
(33, 'I do not know ___ he will come tonight.', 1, 2, 3, 'B', 'whether introduces a noun clause after "know".', 10, 'manual'),
(34, 'If I ___ more confident, I would give the speech in English.', 1, 2, 4, 'C', 'In an unreal present condition, "were" is used for all persons.', 10, 'manual'),
(35, 'I wish I ___ more time to practice speaking every day.', 1, 2, 4, 'B', 'wish followed by a past form expresses an unreal present situation.', 10, 'manual'),
(36, 'In a reading passage, the topic sentence usually helps you identify ___.', 1, 1, 5, 'B', 'The topic sentence usually tells the main idea of a paragraph.', 10, 'manual'),
(37, 'Which strategy is most helpful when you meet an unfamiliar word in a passage?', 1, 1, 5, 'B', 'Context clues help readers infer the meaning of unknown words.', 10, 'manual'),
(38, 'A cloze test mainly checks your ability to understand ___.', 1, 2, 6, 'B', 'A cloze task relies on grammar, logic and context together.', 10, 'manual'),
(39, 'The best clue for choosing the right word in a cloze test is often ___.', 1, 2, 6, 'A', 'Sentence logic and collocation often lead to the correct choice.', 10, 'manual'),
(40, 'Translate: "有效复习能帮助学生长期记忆单词。"', 4, 2, 10, 'Effective review can help students remember words for a long time.', 'The translation keeps both the idea of effective review and long-term memory.', 10, 'manual'),
(41, 'Translate: "阅读时不要只关注生词，也要理解文章结构。"', 4, 2, 10, 'When reading, do not focus only on new words; also understand the structure of the passage.', 'The sentence stresses both vocabulary and passage structure.', 10, 'manual'),
(42, 'Choose the best translation for "她通过不断练习提高了写作能力。"', 1, 2, 10, 'A', 'The sentence uses a natural expression for improving writing ability through practice.', 10, 'manual'),
(43, 'Choose the best translation for "这个计划的关键在于坚持和反馈。"', 1, 2, 10, 'B', 'The key to the plan lies in persistence and feedback is the most natural translation.', 10, 'manual'),
(44, 'Which sentence works best as a topic sentence for a paragraph about online learning?', 1, 1, 7, 'A', 'A topic sentence should clearly present the central idea.', 10, 'manual'),
(45, 'Which connector is best for showing contrast in writing?', 1, 1, 7, 'B', 'however is commonly used to show contrast.', 10, 'manual'),
(46, 'A good conclusion paragraph should mainly ___.', 1, 1, 7, 'C', 'A conclusion should restate the main point and leave a final impression.', 10, 'manual'),
(47, 'Which outline order is the most logical for a short essay?', 1, 1, 7, 'B', 'Most essays follow introduction, body and conclusion.', 10, 'manual'),
(48, 'Before listening to a conversation, the most useful first step is to ___.', 1, 1, 8, 'A', 'Previewing the questions helps you listen with a purpose.', 10, 'manual'),
(49, 'In listening practice, signal words such as "but" or "however" often introduce ___.', 1, 2, 8, 'B', 'These words usually signal a contrast or important shift.', 10, 'manual'),
(50, 'If you miss one detail during listening, you should ___.', 1, 1, 8, 'C', 'Do not get stuck; continue listening for the next key point.', 10, 'manual'),
(51, 'What is the best strategy for note-taking in listening class?', 1, 1, 8, 'B', 'Key words and numbers are more useful than writing every word.', 10, 'manual'),
(52, 'When answering an oral question, it is best to start with ___.', 1, 1, 9, 'B', 'A short direct response helps the listener follow your answer.', 10, 'manual'),
(53, 'Which expression is most suitable for giving an opinion?', 1, 1, 9, 'A', 'In my opinion is a standard phrase for stating an opinion.', 10, 'manual'),
(54, 'If you need extra time in a speaking test, a natural filler is ___.', 1, 1, 9, 'A', 'A natural filler can buy time without breaking communication.', 10, 'manual'),
(55, 'Good pronunciation practice should pay attention to ___.', 1, 1, 9, 'B', 'Both accuracy and fluency are important in spoken English.', 10, 'manual'),
(56, 'The author probably mentions an example in order to ___.', 1, 2, 5, 'B', 'Examples are usually used to support the writer''s point.', 10, 'manual'),
(57, 'Choose the best word: Students should ___ a positive attitude toward mistakes.', 1, 1, 1, 'A', 'maintain is the correct collocation with attitude.', 10, 'manual'),
(58, 'If she had started earlier, she ___ the report on time.', 1, 3, 4, 'C', 'This is a third conditional sentence about an unreal past.', 10, 'manual'),
(59, 'Which sentence is more formal for academic writing?', 1, 2, 7, 'B', 'Option B uses a more formal and objective style.', 10, 'manual'),
(60, 'While listening to a lecture, repeated ideas usually signal ___.', 1, 2, 8, 'B', 'Repetition often shows the speaker wants to emphasize a key point.', 10, 'manual'),
(61, 'To make your spoken answer clearer, you should ___.', 1, 1, 9, 'B', 'Simple linking words help listeners follow your ideas.', 10, 'manual')
ON DUPLICATE KEY UPDATE
`title` = VALUES(`title`),
`type` = VALUES(`type`),
`difficulty` = VALUES(`difficulty`),
`knowledge_id` = VALUES(`knowledge_id`),
`answer` = VALUES(`answer`),
`analysis` = VALUES(`analysis`),
`score` = VALUES(`score`),
`source` = VALUES(`source`);

DELETE FROM `question_option` WHERE `question_id` BETWEEN 26 AND 61;

INSERT INTO `question_option` (`question_id`, `option_key`, `option_value`)
VALUES
(26, 'A', 'reduce'), (26, 'B', 'keep'), (26, 'C', 'hide'), (26, 'D', 'discover'),
(27, 'A', 'slight'), (27, 'B', 'repeated'), (27, 'C', 'important'), (27, 'D', 'uncertain'),
(28, 'A', 'decline'), (28, 'B', 'improve'), (28, 'C', 'translate'), (28, 'D', 'remove'),
(29, 'A', 'save'), (29, 'B', 'use up'), (29, 'C', 'summarize'), (29, 'D', 'compare'),
(30, 'A', 'learn'), (30, 'B', 'learned'), (30, 'C', 'have learned'), (30, 'D', 'will learn'),
(31, 'A', 'sit'), (31, 'B', 'sat'), (31, 'C', 'are sitting'), (31, 'D', 'were sitting'),
(32, 'A', 'The book who I bought yesterday is useful.'), (32, 'B', 'The book that I bought yesterday is useful.'), (32, 'C', 'The book what I bought yesterday is useful.'), (32, 'D', 'The book where I bought yesterday is useful.'),
(33, 'A', 'that'), (33, 'B', 'whether'), (33, 'C', 'because'), (33, 'D', 'than'),
(34, 'A', 'am'), (34, 'B', 'was'), (34, 'C', 'were'), (34, 'D', 'be'),
(35, 'A', 'have'), (35, 'B', 'had'), (35, 'C', 'has'), (35, 'D', 'having'),
(36, 'A', 'the writer''s handwriting'), (36, 'B', 'the main idea of a paragraph'), (36, 'C', 'the number of examples'), (36, 'D', 'the meaning of every new word'),
(37, 'A', 'Stop reading immediately.'), (37, 'B', 'Guess its meaning from context.'), (37, 'C', 'Skip the whole passage.'), (37, 'D', 'Translate every line literally.'),
(38, 'A', 'isolated letters'), (38, 'B', 'grammar and context together'), (38, 'C', 'only pronunciation'), (38, 'D', 'handwriting speed'),
(39, 'A', 'sentence logic and collocation'), (39, 'B', 'the number of letters'), (39, 'C', 'the font size'), (39, 'D', 'the title color'),
(42, 'A', 'She improved her writing ability through continuous practice.'), (42, 'B', 'She was improved by writing every day continuously.'), (42, 'C', 'She improved her write ability with continuity.'), (42, 'D', 'She had writing ability because of practice continuously.'),
(43, 'A', 'The plan is key for insisting and feedback.'), (43, 'B', 'The key to this plan lies in persistence and feedback.'), (43, 'C', 'This plan keys persistence into feedback.'), (43, 'D', 'Feedback persists because of the plan.'),
(44, 'A', 'Online learning offers students greater flexibility and wider access to resources.'), (44, 'B', 'Many students stay up late before exams.'), (44, 'C', 'Some websites contain short videos.'), (44, 'D', 'Teachers often check homework carefully.'),
(45, 'A', 'because'), (45, 'B', 'however'), (45, 'C', 'therefore'), (45, 'D', 'for example'),
(46, 'A', 'introduce a new major argument'), (46, 'B', 'repeat the title only'), (46, 'C', 'restate the main point and leave a final impression'), (46, 'D', 'list unrelated examples'),
(47, 'A', 'examples-introduction-conclusion-body'), (47, 'B', 'introduction-body-conclusion'), (47, 'C', 'conclusion-body-introduction'), (47, 'D', 'body-conclusion-introduction'),
(48, 'A', 'read the questions quickly'), (48, 'B', 'memorize the dictionary'), (48, 'C', 'translate every option'), (48, 'D', 'close your eyes'),
(49, 'A', 'the speaker''s name'), (49, 'B', 'an important change or contrast'), (49, 'C', 'background noise'), (49, 'D', 'a spelling rule'),
(50, 'A', 'stop and think for a long time'), (50, 'B', 'give up the whole task'), (50, 'C', 'continue listening for the next key point'), (50, 'D', 'replay it in your head only'),
(51, 'A', 'write every word down'), (51, 'B', 'note key words and numbers'), (51, 'C', 'copy the textbook'), (51, 'D', 'write in full sentences only'),
(52, 'A', 'complete silence'), (52, 'B', 'a short direct response'), (52, 'C', 'a dictionary definition'), (52, 'D', 'an unrelated joke'),
(53, 'A', 'In my opinion...'), (53, 'B', 'Once upon a time...'), (53, 'C', 'Long time no see...'), (53, 'D', 'See you later...'),
(54, 'A', 'I mean, let me think...'), (54, 'B', 'Whatever!'), (54, 'C', 'Never mind the question.'), (54, 'D', 'No answer.'),
(55, 'A', 'volume only'), (55, 'B', 'accuracy and fluency'), (55, 'C', 'speed only'), (55, 'D', 'grammar only'),
(56, 'A', 'confuse the reader'), (56, 'B', 'support the main idea'), (56, 'C', 'change the topic completely'), (56, 'D', 'list every detail'),
(57, 'A', 'maintain'), (57, 'B', 'abandon'), (57, 'C', 'translate'), (57, 'D', 'accuse'),
(58, 'A', 'finishes'), (58, 'B', 'finished'), (58, 'C', 'would have finished'), (58, 'D', 'will finish'),
(59, 'A', 'Kids nowadays love phones.'), (59, 'B', 'Many young people are highly dependent on smartphones.'), (59, 'C', 'Phones are super cool.'), (59, 'D', 'You know, phones change everything.'),
(60, 'A', 'an unimportant point'), (60, 'B', 'a key point the speaker wants to emphasize'), (60, 'C', 'the end of the exam'), (60, 'D', 'background music'),
(61, 'A', 'use one long sentence only'), (61, 'B', 'organize ideas with simple linking words'), (61, 'C', 'speak as fast as possible'), (61, 'D', 'avoid examples');

DELETE qo1
FROM `question_option` qo1
INNER JOIN `question_option` qo2
  ON qo1.`question_id` = qo2.`question_id`
 AND qo1.`option_key` = qo2.`option_key`
 AND qo1.`option_value` = qo2.`option_value`
 AND qo1.`id` > qo2.`id`;
