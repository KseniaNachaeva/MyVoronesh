-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: localhost
-- Время создания: Янв 29 2026 г., 14:36
-- Версия сервера: 10.4.32-MariaDB
-- Версия PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `MyVoronesh`
--

DELIMITER $$
--
-- Процедуры
--
DROP PROCEDURE IF EXISTS `sp_update_quest_progress`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_update_quest_progress` (IN `p_user_id` VARCHAR(36), IN `p_quest_id` VARCHAR(50))   BEGIN
    DECLARE v_completed INT;
    DECLARE v_total INT;
    
    -- Считаем посещённые точки
    SELECT COUNT(*) INTO v_completed
    FROM user_point_progress upp
    INNER JOIN quest_points qp ON upp.point_id = qp.id
    WHERE upp.user_id = p_user_id 
      AND qp.quest_id = p_quest_id 
      AND upp.visited = 1;
    
    -- Общее количество точек в квесте
    SELECT COUNT(*) INTO v_total
    FROM quest_points
    WHERE quest_id = p_quest_id;
    
    -- Обновляем прогресс
    UPDATE user_quest_progress
    SET completed_points = v_completed,
        completed_at = CASE WHEN v_completed = v_total THEN NOW() ELSE NULL END,
        last_activity_at = NOW()
    WHERE user_id = p_user_id AND quest_id = p_quest_id;
END$$

DROP PROCEDURE IF EXISTS `sp_visit_point`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_visit_point` (IN `p_user_id` VARCHAR(36), IN `p_point_id` VARCHAR(36))   BEGIN
    DECLARE v_quest_id VARCHAR(50);
    
    -- Получаем ID квеста
    SELECT quest_id INTO v_quest_id FROM quest_points WHERE id = p_point_id;
    
    -- Добавляем или обновляем запись о посещении
    INSERT INTO user_point_progress (user_id, point_id, visited, visited_at)
    VALUES (p_user_id, p_point_id, TRUE, NOW())
    ON DUPLICATE KEY UPDATE visited = TRUE, visited_at = NOW();
    
    -- Обновляем прогресс по квесту
    CALL sp_update_quest_progress(p_user_id, v_quest_id);
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Структура таблицы `achievements`
--

DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `id` varchar(50) NOT NULL COMMENT 'ID достижения',
  `title` varchar(100) NOT NULL COMMENT 'Название',
  `description` text DEFAULT NULL COMMENT 'Описание',
  `icon_url` varchar(255) DEFAULT NULL COMMENT 'URL иконки',
  `points_required` int(11) DEFAULT 0 COMMENT 'Требуемые баллы',
  `quests_required` int(11) DEFAULT 0 COMMENT 'Требуемые квесты',
  `points_reward` int(11) DEFAULT 0 COMMENT 'Награда в баллах',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Достижения';

--
-- Дамп данных таблицы `achievements`
--

INSERT INTO `achievements` (`id`, `title`, `description`, `icon_url`, `points_required`, `quests_required`, `points_reward`, `created_at`) VALUES
('art_critic', 'Арт-критик', 'Завершите квест \"Уличное искусство\"', 'achievements/art.png', 0, 0, 100, '2025-12-12 21:02:24'),
('bookworm', 'Книжный червь', 'Завершите квест \"Литература\"', 'achievements/bookworm.png', 0, 0, 150, '2025-12-12 21:02:24'),
('explorer', 'Исследователь', 'Посетите 10 точек', 'achievements/explorer.png', 100, 0, 50, '2025-12-12 21:02:24'),
('first_step', 'Первый шаг', 'Посетите свою первую точку', 'achievements/first_step.png', 0, 0, 10, '2025-12-12 21:02:24'),
('historian', 'Историк', 'Завершите квест \"Архитектура\"', 'achievements/historian.png', 0, 0, 150, '2025-12-12 21:02:24'),
('music_fan', 'Меломан', 'Завершите квест \"Музыкальный квест\"', 'achievements/music.png', 0, 0, 100, '2025-12-12 21:02:24'),
('nature_lover', 'Любитель природы', 'Завершите квест \"Парки и скверы\"', 'achievements/nature.png', 0, 0, 100, '2025-12-12 21:02:24'),
('photographer', 'Фотограф', 'Сделайте 10 фотографий', 'achievements/photographer.png', 0, 0, 30, '2025-12-12 21:02:24'),
('quest_master', 'Мастер квестов', 'Завершите свой первый квест', 'achievements/quest_master.png', 0, 1, 100, '2025-12-12 21:02:24'),
('voronezh_expert', 'Знаток Воронежа', 'Завершите все квесты', 'achievements/expert.png', 0, 6, 500, '2025-12-12 21:02:24');

-- --------------------------------------------------------

--
-- Структура таблицы `quests`
--

DROP TABLE IF EXISTS `quests`;
CREATE TABLE `quests` (
  `id` varchar(50) NOT NULL COMMENT 'ID квеста',
  `title` varchar(100) NOT NULL COMMENT 'Название квеста',
  `description` text DEFAULT NULL COMMENT 'Описание квеста',
  `image_url` varchar(255) DEFAULT NULL COMMENT 'URL изображения',
  `icon_url` varchar(255) DEFAULT NULL COMMENT 'URL иконки',
  `color` varchar(7) DEFAULT '#FFD54F' COMMENT 'Цвет квеста (HEX)',
  `difficulty` enum('easy','medium','hard') DEFAULT 'medium' COMMENT 'Сложность',
  `estimated_time_minutes` int(11) DEFAULT 60 COMMENT 'Примерное время прохождения',
  `order_index` int(11) DEFAULT 0 COMMENT 'Порядок отображения',
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Активен ли квест',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Квесты';

--
-- Дамп данных таблицы `quests`
--

INSERT INTO `quests` (`id`, `title`, `description`, `image_url`, `icon_url`, `color`, `difficulty`, `estimated_time_minutes`, `order_index`, `is_active`, `created_at`) VALUES
('architecture', 'Архитектура', 'Познакомьтесь с архитектурными шедеврами Воронежа. От старинных храмов до советского конструктивизма — город хранит удивительные памятники разных эпох.', 'quests/architecture.jpg', NULL, '#FF9898', 'medium', 120, 1, 1, '2025-12-12 21:02:24'),
('literature', 'Литература', 'Литературные места Воронежа, связанные с писателями, поэтами и героями художественных произведений.', NULL, NULL, '#4B0082', 'medium', 60, 1, 1, '2025-12-14 13:28:08'),
('memory', 'Память', 'Мемориалы, памятники и места, связанные с Великой Отечественной войной и другими войнами. История подвига, героизма и памяти Воронежа.', NULL, NULL, '#8B0000', 'medium', 60, 6, 1, '2025-12-14 13:19:08'),
('music', 'Музыкальный квест', 'Погрузитесь в музыкальную историю Воронежа. От классической музыки до современных концертных площадок.', 'quests/music.jpg', NULL, '#DDA8FF', 'medium', 75, 4, 1, '2025-12-12 21:02:24'),
('parks', 'Парки и скверы', 'Прогуляйтесь по самым красивым зелёным зонам города. Узнайте историю парков и скверов, которые украшают Воронеж уже более века.', 'quests/parks.jpg', NULL, '#FFD188', 'easy', 60, 2, 1, '2025-12-12 21:02:24'),
('street_art', 'Уличное искусство', 'Исследуйте современное искусство на улицах Воронежа. Граффити, муралы и арт-объекты расскажут о городе с неожиданной стороны.', 'quests/street_art.jpg', NULL, '#A8FFA6', 'easy', 45, 3, 1, '2025-12-12 21:02:24');

-- --------------------------------------------------------

--
-- Структура таблицы `quest_points`
--

DROP TABLE IF EXISTS `quest_points`;
CREATE TABLE `quest_points` (
  `id` varchar(36) NOT NULL COMMENT 'UUID точки',
  `quest_id` varchar(50) NOT NULL COMMENT 'ID квеста',
  `title` varchar(150) NOT NULL COMMENT 'Название точки',
  `description` text NOT NULL COMMENT 'Описание точки',
  `short_description` varchar(255) DEFAULT NULL COMMENT 'Краткое описание',
  `latitude` decimal(10,8) NOT NULL COMMENT 'Широта',
  `longitude` decimal(11,8) NOT NULL COMMENT 'Долгота',
  `address` varchar(255) DEFAULT NULL COMMENT 'Адрес',
  `image_url` varchar(255) DEFAULT NULL COMMENT 'URL изображения',
  `audio_url` varchar(255) DEFAULT NULL COMMENT 'URL аудиогида',
  `order_index` int(11) DEFAULT 0 COMMENT 'Порядок в квесте',
  `points_reward` int(11) DEFAULT 10 COMMENT 'Награда в баллах',
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Активна ли точка',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Точки квестов';

--
-- Дамп данных таблицы `quest_points`
--

INSERT INTO `quest_points` (`id`, `quest_id`, `title`, `description`, `short_description`, `latitude`, `longitude`, `address`, `image_url`, `audio_url`, `order_index`, `points_reward`, `is_active`, `created_at`) VALUES
('78e6bfea-d8ef-11f0-b4b0-d88083cf072b', 'memory', 'Памятник Славы', 'Один из главных мемориалов Воронежа, посвящённый защитникам города в годы Великой Отечественной войны. Здесь проходят памятные мероприятия и возложения цветов.', 'Главный военный мемориал Воронежа', 51.70357000, 39.18239400, 'Московский проспект, Воронеж', 'uploads/points/memory_glory.jpg', NULL, 1, 10, 1, '2025-12-14 13:19:08'),
('78e6c231-d8ef-11f0-b4b0-d88083cf072b', 'memory', 'Площадь Победы', 'Центральный мемориальный комплекс Воронежа. Ансамбль символизирует единство фронта и тыла и посвящён подвигу народа в годы Великой Отечественной войны.', 'Центральный мемориальный комплекс города', 51.67280000, 39.20680000, 'Площадь Победы, Воронеж', 'uploads/points/memory_victory_square.jpg', NULL, 2, 10, 1, '2025-12-14 13:19:08'),
('78e6c2c9-d8ef-11f0-b4b0-d88083cf072b', 'memory', 'Чижовский плацдарм', 'Крупнейший мемориальный комплекс, посвящённый одному из самых кровопролитных сражений за Воронеж. Здесь захоронены тысячи советских солдат.', 'Место одного из ключевых сражений за Воронеж', 51.64380000, 39.18850000, 'Чижовка, Воронеж', 'uploads/points/memory_chizhovka.jpg', NULL, 4, 10, 1, '2025-12-14 13:19:08'),
('78e6c301-d8ef-11f0-b4b0-d88083cf072b', 'memory', 'Братская могила №6', 'Место захоронения советских воинов, погибших при защите и освобождении Воронежа. Один из важных памятников военной истории города.', 'Место захоронения советских солдат', 51.69750000, 39.17930000, 'ул. Лизюкова, Воронеж', 'uploads/points/memory_mass_grave.jpg', NULL, 5, 10, 1, '2025-12-14 13:19:08'),
('78e6c34f-d8ef-11f0-b4b0-d88083cf072b', 'memory', 'Памятник воинам-интернационалистам', 'Мемориал, посвящённый жителям Воронежской области, погибшим при выполнении воинского долга за пределами страны.', 'Память о воронежцах, погибших за пределами Родины', 51.66630000, 39.21360000, 'ул. Кирова, Воронеж', 'uploads/points/memory_internationalists.jpg', NULL, 6, 10, 1, '2025-12-14 13:19:08'),
('arch_1', 'architecture', 'Успенский Адмиралтейский храм', 'Старейший сохранившийся храм Воронежа, построенный в XVII веке. Связан с историей российского флота — здесь освящались корабли, построенные на воронежских верфях при Петре I. Храм является памятником архитектуры федерального значения.', 'Старейший храм города, свидетель петровской эпохи', 51.66860000, 39.21150000, 'ул. Софьи Перовской, 9', 'uploads/points/uspenskiy.jpg', NULL, 0, 20, 1, '2025-12-12 21:02:24'),
('arch_10', 'architecture', 'Благовещенский кафедральный собор', 'Крупнейший православный храм Воронежа, построенный в русско-византийском стиле. Строительство завершено в 2009 году. Высота колокольни — 97 метров, это третий по величине православный храм в России.', 'Крупнейший храм города, построенный в XXI веке', 51.67510000, 39.21020000, 'пр. Революции, 14А', 'uploads/points/blagoveshenskiy.jpg', NULL, 9, 20, 1, '2025-12-12 21:02:24'),
('arch_11', 'architecture', 'Дом купца Клочкова', 'Особняк в стиле эклектики, построенный во второй половине XIX века. Принадлежал известному воронежскому купцу. Фасад украшен богатым декором.', 'Купеческий особняк XIX века', 51.66540000, 39.20130000, 'ул. Плехановская, 10', 'uploads/points/klochkov.jpg', NULL, 10, 15, 1, '2025-12-12 21:02:24'),
('arch_12', 'architecture', 'Воронежский областной краеведческий музей', 'Здание музея — памятник архитектуры XIX века. Музей основан в 1894 году и хранит богатейшую коллекцию по истории и природе Воронежского края.', 'Главный музей истории Воронежского края', 51.66500000, 39.20170000, 'пл. Ленина, 4', 'uploads/points/museum.jpg', NULL, 11, 15, 1, '2025-12-12 21:02:24'),
('arch_2', 'architecture', 'Каменный мост', 'Первый каменный мост в Воронеже, построенный в 1826 году по проекту архитектора И.А. Блицына. Мост соединяет улицы Карла Маркса и Чернышевского. Является памятником архитектуры и одним из символов города.', 'Первый каменный мост города, памятник XIX века', 51.67250000, 39.21080000, 'ул. Карла Маркса', 'uploads/points/bridge.jpg', NULL, 1, 15, 1, '2025-12-12 21:02:24'),
('arch_3', 'architecture', 'Здание управления ЮВЖД', 'Монументальное здание в стиле сталинского ампира, построенное в 1952 году. Является штаб-квартирой Юго-Восточной железной дороги. Архитектура здания отражает послевоенный оптимизм и веру в будущее.', 'Образец сталинского ампира, штаб-квартира ЮВЖД', 51.66960000, 39.20310000, 'пр. Революции, 18', 'uploads/points/railway.jpg', NULL, 2, 15, 1, '2025-12-12 21:02:24'),
('arch_4', 'architecture', 'Дом губернатора', 'Историческое здание XVIII века, служившее резиденцией воронежских губернаторов. Образец русского классицизма. Здесь принимались важные решения, влиявшие на судьбу губернии.', 'Бывшая резиденция воронежских губернаторов', 51.66930000, 39.20440000, 'пр. Революции, 22', 'uploads/points/governor.jpg', NULL, 3, 15, 1, '2025-12-12 21:02:24'),
('arch_5', 'architecture', 'Покровский кафедральный собор', 'Главный православный храм Воронежа, построенный в XVIII веке в стиле барокко. Собор неоднократно перестраивался, но сохранил величественный облик. Здесь находятся мощи святителя Митрофана Воронежского.', 'Главный храм Воронежской епархии', 51.67200000, 39.21900000, 'ул. Бехтерева, 36', 'uploads/points/pokrovskiy.jpg', NULL, 4, 20, 1, '2025-12-12 21:02:24'),
('arch_6', 'architecture', 'Гостиница \"Бристоль\"', 'Здание в стиле модерн, построенное в начале XX века. Один из лучших образцов архитектуры модерна в Воронеже. Гостиница славилась комфортом и принимала именитых гостей города.', 'Памятник архитектуры модерна начала XX века', 51.66560000, 39.20250000, 'пр. Революции, 43', 'uploads/points/bristol.jpg', NULL, 5, 15, 1, '2025-12-12 21:02:24'),
('arch_7', 'architecture', 'Здание Мариинской гимназии', 'Историческое здание женской гимназии, построенное в XIX веке. Образец учебного заведения дореволюционной России. Здание отличается строгой классической архитектурой.', 'Бывшая женская гимназия XIX века', 51.66830000, 39.20480000, 'ул. Сакко и Ванцетти, 102', 'uploads/points/gymnasium.jpg', NULL, 6, 15, 1, '2025-12-12 21:02:24'),
('arch_8', 'architecture', 'Дом Вигеля', 'Памятник архитектуры XVIII века, связанный с именем мемуариста Ф.Ф. Вигеля. Дом является образцом городской усадьбы эпохи классицизма.', 'Городская усадьба XVIII века', 51.66690000, 39.20730000, 'ул. Комиссаржевской, 15', 'uploads/points/vigel.jpg', NULL, 7, 15, 1, '2025-12-12 21:02:24'),
('arch_9', 'architecture', 'Кинотеатр \"Пролетарий\"', 'Здание в стиле конструктивизма, построенное в 1930-х годах. Один из первых звуковых кинотеатров города. Образец советской архитектуры авангарда.', 'Памятник советского конструктивизма', 51.66230000, 39.19940000, 'пр. Революции, 56', 'uploads/points/proletariy.jpg', NULL, 8, 15, 1, '2025-12-12 21:02:24'),
('art_1', 'street_art', 'Граффити на Плехановской', 'Масштабное граффити на стене жилого дома, созданное в рамках фестиваля уличного искусства. Работа посвящена истории и культуре Воронежа.', 'Масштабный мурал на стене жилого дома', 51.66520000, 39.19890000, 'ул. Плехановская, 45', 'uploads/points/graffiti1.jpg', NULL, 0, 15, 1, '2025-12-12 21:02:24'),
('art_2', 'street_art', 'Арт-объект \"Котёнок с улицы Лизюкова\"', 'Скульптура героя знаменитого мультфильма, созданного воронежским режиссёром Вячеславом Котёночкиным. Один из самых фотографируемых объектов города.', 'Скульптура героя знаменитого мультфильма', 51.70360000, 39.15780000, 'ул. Генерала Лизюкова', 'uploads/points/kotyonok.jpg', NULL, 1, 20, 1, '2025-12-12 21:02:24'),
('art_3', 'street_art', 'Мурал \"Платонов\"', 'Портрет писателя Андрея Платонова на стене здания. Работа уличного художника, посвящённая знаменитому уроженцу Воронежа.', 'Портрет писателя А. Платонова на стене здания', 51.67090000, 39.20970000, 'ул. Карла Маркса, 31', 'uploads/points/platonov_mural.jpg', NULL, 2, 15, 1, '2025-12-12 21:02:24'),
('art_4', 'street_art', 'Стрит-арт квартал', 'Комплекс стен с работами уличных художников. Здесь можно увидеть произведения в разных стилях — от реализма до абстракции.', 'Квартал с работами уличных художников', 51.66450000, 39.19570000, 'ул. 9 Января', 'uploads/points/street_quarter.jpg', NULL, 3, 15, 1, '2025-12-12 21:02:24'),
('art_5', 'street_art', 'Арт-объект \"Стул\"', 'Необычная скульптура гигантского стула, ставшая точкой притяжения для туристов. Пример современного городского искусства.', 'Скульптура гигантского стула', 51.66550000, 39.20250000, 'Советская пл.', 'uploads/points/chair.jpg', NULL, 4, 10, 1, '2025-12-12 21:02:24'),
('bac197f5-d8f0-11f0-b4b0-d88083cf072b', 'literature', 'Памятник Ивану Бунину', 'Памятник нобелевскому лауреату Ивану Алексеевичу Бунину, родившемуся в Воронеже. Символ литературного наследия города.', 'Памятник писателю Ивану Бунину', 51.67190000, 39.20890000, 'проспект Революции, Воронеж', 'uploads/points/lit_bunin.jpg', NULL, 1, 10, 1, '2025-12-14 13:28:08'),
('bac199d4-d8f0-11f0-b4b0-d88083cf072b', 'literature', 'Памятник Самуилу Маршаку', 'Памятник детскому писателю Самуилу Яковлевичу Маршаку, чьё творчество тесно связано с Воронежем.', 'Памятник Самуилу Маршаку', 51.67240000, 39.21180000, 'проспект Революции, Воронеж', 'uploads/points/lit_marshak.jpg', NULL, 2, 10, 1, '2025-12-14 13:28:08'),
('bac19a39-d8f0-11f0-b4b0-d88083cf072b', 'literature', 'Дом-музей Ивана Никитина', 'Дом, в котором жил поэт Иван Саввич Никитин. Сейчас здесь расположен литературный музей.', 'Дом-музей поэта Ивана Никитина', 51.66490000, 39.20280000, 'ул. Никитинская, 19, Воронеж', 'uploads/points/lit_nikitin.jpg', NULL, 3, 10, 1, '2025-12-14 13:28:08'),
('bac19a79-d8f0-11f0-b4b0-d88083cf072b', 'literature', 'Библиотека им. И.С. Никитина', 'Крупнейшая библиотека Воронежской области, названная в честь поэта Ивана Никитина.', 'Главная библиотека региона', 51.66580000, 39.20340000, 'пл. Ленина, 2, Воронеж', 'uploads/points/lit_library.jpg', NULL, 4, 10, 1, '2025-12-14 13:28:08'),
('mus_1', 'music', 'Воронежский театр оперы и балета', 'Главный музыкальный театр города, основанный в 1931 году. На сцене театра идут классические оперы и балеты, а также современные постановки. Здание театра — памятник советской архитектуры.', 'Главный музыкальный театр Воронежа', 51.65980000, 39.20070000, 'пл. Ленина, 7', 'uploads/points/opera.jpg', NULL, 0, 20, 1, '2025-12-12 21:02:24'),
('mus_2', 'music', 'Воронежская филармония', 'Концертный зал, основанный в 1937 году. Здесь выступают ведущие музыканты России и мира. Филармония славится отличной акустикой и богатой концертной программой.', 'Концертный зал с мировой акустикой', 51.66640000, 39.20350000, 'пл. Ленина, 11А', 'uploads/points/philharmonic.jpg', NULL, 1, 15, 1, '2025-12-12 21:02:24'),
('mus_3', 'music', 'Дом актёра', 'Культурный центр, где проходят камерные концерты, творческие вечера, встречи с артистами. Уютная атмосфера для настоящих ценителей искусства.', 'Площадка для камерных концертов и творческих вечеров', 51.66770000, 39.20620000, 'пр. Революции, 32', 'uploads/points/dom_aktera.jpg', NULL, 2, 10, 1, '2025-12-12 21:02:24'),
('mus_4', 'music', 'Камерный театр', 'Уникальный театр, где музыка является важной частью каждой постановки. Известен экспериментальными спектаклями и особой атмосферой.', 'Театр музыкальных экспериментов', 51.66730000, 39.20750000, 'ул. Карла Маркса, 55А', 'uploads/points/chamber.jpg', NULL, 3, 15, 1, '2025-12-12 21:02:24'),
('mus_5', 'music', 'Концертный зал Event-Hall', 'Современная концертная площадка, принимающая звёзд российской и мировой эстрады. Оснащена по последнему слову техники.', 'Современная концертная площадка', 51.64690000, 39.10990000, 'Московский пр., 129/1', 'uploads/points/event_hall.jpg', NULL, 4, 10, 1, '2025-12-12 21:02:24'),
('park_1', 'parks', 'Кольцовский сквер', 'Старейший сквер Воронежа, разбитый в 1868 году на месте бывшей Хлебной площади. Назван в честь поэта А.В. Кольцова, памятник которому установлен в центре. Сквер украшен музыкальным фонтаном, это любимое место отдыха горожан.', 'Старейший сквер города с памятником поэту А.В. Кольцову', 51.66590000, 39.20210000, 'ул. Плехановская', 'uploads/points/koltsov_park.jpg', NULL, 0, 15, 1, '2025-12-12 21:02:24'),
('park_2', 'parks', 'Петровский сквер', 'Сквер у памятника Петру I — основателю российского флота. Памятник установлен в 1860 году и является одним из старейших в городе. Отсюда открывается прекрасный вид на водохранилище.', 'Сквер с памятником Петру I', 51.67220000, 39.21310000, 'пр. Революции', 'uploads/points/petrov_park.jpg', NULL, 1, 15, 1, '2025-12-12 21:02:24'),
('park_3', 'parks', 'Центральный парк культуры и отдыха', 'Главный парк Воронежа, основанный в XIX веке. Расположен на высоком берегу водохранилища. В парке есть аттракционы, кафе, смотровые площадки с видом на левый берег.', 'Главный парк города с видом на водохранилище', 51.67530000, 39.20780000, 'ул. Ленина, 10', 'uploads/points/central_park.jpg', NULL, 2, 15, 1, '2025-12-12 21:02:24'),
('park_4', 'parks', 'Парк \"Алые паруса\"', 'Современный парк на берегу водохранилища, открытый в 2011 году. Назван в честь повести А. Грина. Парк оборудован пляжем, спортивными площадками, детскими зонами.', 'Современный парк на берегу водохранилища', 51.68950000, 39.17770000, 'ул. Арзамасская, 4А', 'uploads/points/alye_parusa.jpg', NULL, 3, 15, 1, '2025-12-12 21:02:24'),
('park_5', 'parks', 'Парк \"Орлёнок\"', 'Детский парк в центре города, основанный в 1954 году. Популярное место семейного отдыха с аттракционами и игровыми зонами.', 'Популярный детский парк в центре города', 51.66850000, 39.20470000, 'ул. Феоктистова, 1', 'uploads/points/orlyonok.jpg', NULL, 4, 10, 1, '2025-12-12 21:02:24'),
('park_6', 'parks', 'Сквер у памятника Славы', 'Мемориальный сквер, посвящённый воинам Великой Отечественной войны. Здесь горит Вечный огонь и находится братская могила защитников города.', 'Мемориальный сквер с Вечным огнём', 51.65930000, 39.19860000, 'ул. Хользунова', 'uploads/points/slava_park.jpg', NULL, 5, 15, 1, '2025-12-12 21:02:24'),
('park_7', 'parks', 'Ботанический сад ВГУ', 'Научный и рекреационный комплекс площадью более 70 гектаров. Коллекция насчитывает более 3000 видов растений со всего мира. Прекрасное место для прогулок и знакомства с природой.', 'Крупнейший ботанический сад Черноземья', 51.65990000, 39.23810000, 'ул. Морозова, 1', 'uploads/points/botanical.jpg', NULL, 6, 20, 1, '2025-12-12 21:02:24'),
('park_8', 'parks', 'Парк \"Динамо\"', 'Спортивный парк с богатой историей. Здесь расположены спортивные площадки, беговые дорожки, зоны для отдыха. Любимое место спортсменов и активных горожан.', 'Спортивный парк для активного отдыха', 51.67890000, 39.19230000, 'ул. Дзержинского', 'uploads/points/dinamo.jpg', NULL, 7, 10, 1, '2025-12-12 21:02:24');

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` varchar(36) NOT NULL COMMENT 'UUID пользователя',
  `login` varchar(50) NOT NULL COMMENT 'Логин для входа',
  `password_hash` varchar(255) NOT NULL COMMENT 'Хеш пароля (bcrypt)',
  `email` varchar(100) DEFAULT NULL COMMENT 'Email пользователя',
  `name` varchar(100) NOT NULL COMMENT 'Отображаемое имя',
  `birth_date` date DEFAULT NULL COMMENT 'Дата рождения',
  `avatar_url` varchar(255) DEFAULT NULL COMMENT 'URL аватара',
  `is_active` tinyint(1) DEFAULT 1 COMMENT 'Активен ли аккаунт',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp() COMMENT 'Дата регистрации',
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp() COMMENT 'Дата обновления',
  `last_login_at` timestamp NULL DEFAULT NULL COMMENT 'Последний вход'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Пользователи приложения';

--
-- Дамп данных таблицы `users`
--

INSERT INTO `users` (`id`, `login`, `password_hash`, `email`, `name`, `birth_date`, `avatar_url`, `is_active`, `created_at`, `updated_at`, `last_login_at`) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'test_user1', '$2a$10$V1dRZvZcd2n.NZSaLSCNzOQFd3iW8i2.4xzCnfboQqkTefIhVCkUW', 'test1@example.cot', 'Алексей Петроvv', '2019-11-10', 'uploads/avatars/550e8400-e29b-41d4-a716-446655440001.jpg', 1, '2024-01-15 07:00:00', '2026-01-29 13:30:51', '2025-12-18 19:12:48'),
('550e8400-e29b-41d4-a716-446655440002', 'test_user2', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'test2@example.com', 'Мария Иванова', '1998-11-22', 'uploads/avatars/550e8400-e29b-41d4-a716-446655440002.png', 1, '2024-06-01 11:30:00', '2025-12-13 16:08:27', '2025-12-13 16:08:27');

-- --------------------------------------------------------

--
-- Структура таблицы `user_achievements`
--

DROP TABLE IF EXISTS `user_achievements`;
CREATE TABLE `user_achievements` (
  `user_id` varchar(36) NOT NULL,
  `achievement_id` varchar(50) NOT NULL,
  `earned_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Полученные достижения';

--
-- Дамп данных таблицы `user_achievements`
--

INSERT INTO `user_achievements` (`user_id`, `achievement_id`, `earned_at`) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'explorer', '2024-02-01 09:30:00'),
('550e8400-e29b-41d4-a716-446655440001', 'first_step', '2024-01-20 09:30:00'),
('550e8400-e29b-41d4-a716-446655440001', 'photographer', '2024-04-11 11:15:00'),
('550e8400-e29b-41d4-a716-446655440002', 'first_step', '2024-06-05 11:30:00');

-- --------------------------------------------------------

--
-- Структура таблицы `user_photos`
--

DROP TABLE IF EXISTS `user_photos`;
CREATE TABLE `user_photos` (
  `id` varchar(36) NOT NULL COMMENT 'UUID фото',
  `user_id` varchar(36) NOT NULL COMMENT 'ID пользователя',
  `point_id` varchar(36) NOT NULL COMMENT 'ID точки',
  `photo_url` varchar(255) NOT NULL COMMENT 'URL фотографии',
  `thumbnail_url` varchar(255) DEFAULT NULL COMMENT 'URL миниатюры',
  `description` text DEFAULT NULL COMMENT 'Описание фото',
  `is_public` tinyint(1) DEFAULT 0 COMMENT 'Публичное ли фото',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Фотографии пользователей';

--
-- Дамп данных таблицы `user_photos`
--

INSERT INTO `user_photos` (`id`, `user_id`, `point_id`, `photo_url`, `thumbnail_url`, `description`, `is_public`, `created_at`) VALUES
('08c7f2d0-26a9-4869-9651-23ffd8e11eab', '550e8400-e29b-41d4-a716-446655440001', '78e6bfea-d8ef-11f0-b4b0-d88083cf072b', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/08c7f2d0-26a9-4869-9651-23ffd8e11eab.jpg', NULL, 'Памятник Славы', 0, '2025-12-14 16:38:28'),
('433b0842-2feb-41d8-9cd1-206f855ef57c', '550e8400-e29b-41d4-a716-446655440001', 'mus_2', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/433b0842-2feb-41d8-9cd1-206f855ef57c.jpg', NULL, 'Воронежская филармония', 0, '2025-12-14 09:23:30'),
('6d7ea73f-1d5f-4f92-a5c0-6e4e4b312b14', '550e8400-e29b-41d4-a716-446655440001', 'arch_3', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/6d7ea73f-1d5f-4f92-a5c0-6e4e4b312b14.jpg', NULL, '', 0, '2025-12-16 19:08:24'),
('718a1fb3-e925-46e6-8f91-0b520b9d0f3b', '550e8400-e29b-41d4-a716-446655440002', 'park_4', 'uploads/photos/550e8400-e29b-41d4-a716-446655440002/718a1fb3-e925-46e6-8f91-0b520b9d0f3b.png', NULL, 'Парк \"Алые паруса\"', 0, '2025-12-13 16:08:47'),
('77375f5e-db18-4eb2-8d37-2b1628806cbd', '550e8400-e29b-41d4-a716-446655440002', 'art_5', 'uploads/photos/550e8400-e29b-41d4-a716-446655440002/77375f5e-db18-4eb2-8d37-2b1628806cbd.png', NULL, 'Арт-объект \"Стул\"', 0, '2025-12-13 16:19:19'),
('8a5e623c-5f6e-4ddd-a1a4-5210fc30128b', '550e8400-e29b-41d4-a716-446655440001', 'arch_3', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/8a5e623c-5f6e-4ddd-a1a4-5210fc30128b.jpg', NULL, '', 0, '2025-12-16 19:19:49'),
('8b4297a4-7f51-411d-ac2e-b701fcd53b89', '550e8400-e29b-41d4-a716-446655440001', 'bac197f5-d8f0-11f0-b4b0-d88083cf072b', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/8b4297a4-7f51-411d-ac2e-b701fcd53b89.jpg', NULL, 'Памятник Ивану Бунину', 0, '2025-12-25 14:47:29'),
('ae552654-875a-4293-bf56-dae85eed366a', '550e8400-e29b-41d4-a716-446655440001', 'arch_10', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/ae552654-875a-4293-bf56-dae85eed366a.jpg', NULL, 'Благовещенский кафедральный собор', 0, '2025-12-14 21:30:58'),
('b978aced-4fa8-4fd9-bb9a-1869ff02f881', '550e8400-e29b-41d4-a716-446655440001', 'arch_3', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/b978aced-4fa8-4fd9-bb9a-1869ff02f881.jpg', NULL, '', 0, '2025-12-16 19:20:07'),
('dca096e7-f514-48a3-8663-60fe84b9d44b', '550e8400-e29b-41d4-a716-446655440001', 'arch_11', 'uploads/photos/550e8400-e29b-41d4-a716-446655440001/dca096e7-f514-48a3-8663-60fe84b9d44b.jpg', NULL, 'Дом купца Клочкова', 0, '2025-12-14 21:30:28'),
('photo-101', '550e8400-e29b-41d4-a716-446655440002', 'park_1', 'photos/user2/koltsov_1.jpg', NULL, 'Красивый фонтан!', 0, '2024-06-05 11:45:00'),
('photo-103', '550e8400-e29b-41d4-a716-446655440002', 'art_2', 'photos/user2/kotyonok_1.jpg', NULL, 'Котёнок с улицы Лизюкова ❤️', 0, '2024-06-10 09:10:00');

-- --------------------------------------------------------

--
-- Структура таблицы `user_point_progress`
--

DROP TABLE IF EXISTS `user_point_progress`;
CREATE TABLE `user_point_progress` (
  `user_id` varchar(36) NOT NULL COMMENT 'ID пользователя',
  `point_id` varchar(36) NOT NULL COMMENT 'ID точки',
  `visited` tinyint(1) DEFAULT 0 COMMENT 'Посещена ли точка',
  `visited_at` timestamp NULL DEFAULT NULL COMMENT 'Дата посещения',
  `rating` tinyint(4) DEFAULT NULL COMMENT 'Оценка точки (1-5)',
  `notes` text DEFAULT NULL COMMENT 'Заметки пользователя'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Прогресс по точкам';

--
-- Дамп данных таблицы `user_point_progress`
--

INSERT INTO `user_point_progress` (`user_id`, `point_id`, `visited`, `visited_at`, `rating`, `notes`) VALUES
('550e8400-e29b-41d4-a716-446655440001', '78e6bfea-d8ef-11f0-b4b0-d88083cf072b', 1, '2025-12-18 05:46:32', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', '78e6c231-d8ef-11f0-b4b0-d88083cf072b', 0, NULL, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', '78e6c2c9-d8ef-11f0-b4b0-d88083cf072b', 0, NULL, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', '78e6c301-d8ef-11f0-b4b0-d88083cf072b', 1, '2025-12-18 05:45:15', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', '78e6c34f-d8ef-11f0-b4b0-d88083cf072b', 0, NULL, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_1', 1, '2025-12-13 12:02:23', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_10', 1, '2025-12-25 15:08:15', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_11', 1, '2025-12-13 19:32:43', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_12', 1, '2025-12-13 19:32:50', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_2', 1, '2024-02-01 09:30:00', 4, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_3', 1, '2025-12-13 19:32:35', 4, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_4', 0, NULL, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_5', 1, '2024-02-03 11:00:00', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_6', 1, '2025-12-13 19:32:58', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_7', 1, '2025-12-13 19:32:46', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_8', 1, '2025-12-13 19:33:07', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'arch_9', 1, '2025-12-13 19:32:40', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'art_1', 1, '2024-04-10 09:00:00', 4, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'art_2', 1, '2025-12-25 15:11:44', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'bac197f5-d8f0-11f0-b4b0-d88083cf072b', 0, NULL, NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'bac199d4-d8f0-11f0-b4b0-d88083cf072b', 1, '2025-12-14 11:31:43', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'bac19a39-d8f0-11f0-b4b0-d88083cf072b', 1, '2025-12-14 11:31:50', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'bac19a79-d8f0-11f0-b4b0-d88083cf072b', 1, '2025-12-14 11:31:48', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'mus_1', 1, '2025-12-14 14:50:01', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_1', 1, '2024-03-15 07:00:00', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_2', 1, '2024-03-15 09:00:00', 4, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_3', 1, '2024-03-16 12:00:00', 5, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_6', 1, '2025-12-14 07:08:28', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_7', 1, '2025-12-14 07:08:20', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440001', 'park_8', 1, '2025-12-14 07:08:24', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'art_2', 1, '2024-06-10 09:00:00', 5, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'art_5', 1, '2025-12-13 14:19:16', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'park_1', 1, '2024-06-05 11:30:00', 5, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'park_4', 1, '2024-06-06 13:00:00', 5, NULL);

-- --------------------------------------------------------

--
-- Структура таблицы `user_quest_progress`
--

DROP TABLE IF EXISTS `user_quest_progress`;
CREATE TABLE `user_quest_progress` (
  `user_id` varchar(36) NOT NULL COMMENT 'ID пользователя',
  `quest_id` varchar(50) NOT NULL COMMENT 'ID квеста',
  `is_selected` tinyint(1) DEFAULT 0 COMMENT 'Выбран ли квест',
  `is_enabled` tinyint(1) DEFAULT 0 COMMENT 'Включен ли на карте',
  `completed_points` int(11) DEFAULT 0 COMMENT 'Пройденных точек',
  `total_points_earned` int(11) DEFAULT 0 COMMENT 'Заработано баллов',
  `started_at` timestamp NULL DEFAULT NULL COMMENT 'Дата начала',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT 'Дата завершения',
  `last_activity_at` timestamp NULL DEFAULT NULL COMMENT 'Последняя активность'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Прогресс по квестам';

--
-- Дамп данных таблицы `user_quest_progress`
--

INSERT INTO `user_quest_progress` (`user_id`, `quest_id`, `is_selected`, `is_enabled`, `completed_points`, `total_points_earned`, `started_at`, `completed_at`, `last_activity_at`) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'architecture', 0, 0, 11, 0, '2024-02-01 07:00:00', NULL, '2025-12-25 15:08:15'),
('550e8400-e29b-41d4-a716-446655440001', 'literature', 0, 0, 3, 0, '2025-12-14 13:28:54', NULL, '2025-12-25 14:47:19'),
('550e8400-e29b-41d4-a716-446655440001', 'memory', 0, 0, 2, 0, '2025-12-14 13:24:48', NULL, '2025-12-18 07:46:32'),
('550e8400-e29b-41d4-a716-446655440001', 'music', 1, 1, 1, 0, '2025-12-13 13:30:44', NULL, '2025-12-14 16:50:01'),
('550e8400-e29b-41d4-a716-446655440001', 'parks', 1, 1, 6, 0, '2024-03-15 06:00:00', NULL, '2025-12-14 09:08:28'),
('550e8400-e29b-41d4-a716-446655440001', 'street_art', 1, 1, 2, 0, '2024-04-10 08:00:00', NULL, '2025-12-25 15:11:44'),
('550e8400-e29b-41d4-a716-446655440002', 'parks', 1, 0, 2, 0, '2024-06-05 11:00:00', NULL, NULL),
('550e8400-e29b-41d4-a716-446655440002', 'street_art', 1, 1, 2, 0, '2024-06-10 08:00:00', NULL, '2025-12-13 16:19:16');

-- --------------------------------------------------------

--
-- Структура таблицы `user_sessions`
--

DROP TABLE IF EXISTS `user_sessions`;
CREATE TABLE `user_sessions` (
  `id` varchar(36) NOT NULL,
  `user_id` varchar(36) NOT NULL,
  `token` varchar(500) NOT NULL,
  `device_info` varchar(255) DEFAULT NULL COMMENT 'Информация об устройстве',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'IP адрес',
  `expires_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Сессии пользователей';

-- --------------------------------------------------------

--
-- Дублирующая структура для представления `v_leaderboard`
-- (См. Ниже фактическое представление)
--
DROP VIEW IF EXISTS `v_leaderboard`;
CREATE TABLE `v_leaderboard` (
`id` varchar(36)
,`name` varchar(100)
,`avatar_url` varchar(255)
,`visited_points` bigint(21)
,`photos_count` bigint(21)
,`achievements_count` bigint(21)
,`total_score` bigint(24)
);

-- --------------------------------------------------------

--
-- Дублирующая структура для представления `v_quest_stats`
-- (См. Ниже фактическое представление)
--
DROP VIEW IF EXISTS `v_quest_stats`;
CREATE TABLE `v_quest_stats` (
`id` varchar(50)
,`title` varchar(100)
,`total_points` bigint(21)
,`visited_by_users` bigint(21)
,`users_started` bigint(21)
);

-- --------------------------------------------------------

--
-- Дублирующая структура для представления `v_user_full_stats`
-- (См. Ниже фактическое представление)
--
DROP VIEW IF EXISTS `v_user_full_stats`;
CREATE TABLE `v_user_full_stats` (
`user_id` varchar(36)
,`name` varchar(100)
,`login` varchar(50)
,`quests_started` bigint(21)
,`points_visited` bigint(21)
,`total_points` bigint(21)
,`photos_taken` bigint(21)
,`achievements_earned` bigint(21)
);

-- --------------------------------------------------------

--
-- Структура для представления `v_leaderboard`
--
DROP TABLE IF EXISTS `v_leaderboard`;

DROP VIEW IF EXISTS `v_leaderboard`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_leaderboard`  AS SELECT `u`.`id` AS `id`, `u`.`name` AS `name`, `u`.`avatar_url` AS `avatar_url`, count(distinct case when `upp`.`visited` = 1 then `upp`.`point_id` end) AS `visited_points`, count(distinct `up`.`id`) AS `photos_count`, count(distinct `ua`.`achievement_id`) AS `achievements_count`, count(distinct case when `upp`.`visited` = 1 then `upp`.`point_id` end) * 10 + count(distinct `ua`.`achievement_id`) * 50 AS `total_score` FROM (((`users` `u` left join `user_point_progress` `upp` on(`u`.`id` = `upp`.`user_id`)) left join `user_photos` `up` on(`u`.`id` = `up`.`user_id`)) left join `user_achievements` `ua` on(`u`.`id` = `ua`.`user_id`)) GROUP BY `u`.`id`, `u`.`name`, `u`.`avatar_url` ORDER BY count(distinct case when `upp`.`visited` = 1 then `upp`.`point_id` end) * 10 + count(distinct `ua`.`achievement_id`) * 50 DESC ;

-- --------------------------------------------------------

--
-- Структура для представления `v_quest_stats`
--
DROP TABLE IF EXISTS `v_quest_stats`;

DROP VIEW IF EXISTS `v_quest_stats`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_quest_stats`  AS SELECT `q`.`id` AS `id`, `q`.`title` AS `title`, count(distinct `qp`.`id`) AS `total_points`, count(distinct case when `upp`.`visited` = 1 then `upp`.`point_id` end) AS `visited_by_users`, count(distinct `uqp`.`user_id`) AS `users_started` FROM (((`quests` `q` left join `quest_points` `qp` on(`q`.`id` = `qp`.`quest_id`)) left join `user_point_progress` `upp` on(`qp`.`id` = `upp`.`point_id`)) left join `user_quest_progress` `uqp` on(`q`.`id` = `uqp`.`quest_id`)) GROUP BY `q`.`id`, `q`.`title` ;

-- --------------------------------------------------------

--
-- Структура для представления `v_user_full_stats`
--
DROP TABLE IF EXISTS `v_user_full_stats`;

DROP VIEW IF EXISTS `v_user_full_stats`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_user_full_stats`  AS SELECT `u`.`id` AS `user_id`, `u`.`name` AS `name`, `u`.`login` AS `login`, count(distinct `uqp`.`quest_id`) AS `quests_started`, count(distinct case when `upp`.`visited` = 1 then `upp`.`point_id` end) AS `points_visited`, (select count(0) from `quest_points`) AS `total_points`, count(distinct `up`.`id`) AS `photos_taken`, count(distinct `ua`.`achievement_id`) AS `achievements_earned` FROM ((((`users` `u` left join `user_quest_progress` `uqp` on(`u`.`id` = `uqp`.`user_id`)) left join `user_point_progress` `upp` on(`u`.`id` = `upp`.`user_id`)) left join `user_photos` `up` on(`u`.`id` = `up`.`user_id`)) left join `user_achievements` `ua` on(`u`.`id` = `ua`.`user_id`)) GROUP BY `u`.`id`, `u`.`name`, `u`.`login` ;

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `achievements`
--
ALTER TABLE `achievements`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `quests`
--
ALTER TABLE `quests`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `quest_points`
--
ALTER TABLE `quest_points`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_quest_points_quest` (`quest_id`),
  ADD KEY `idx_quest_points_location` (`latitude`,`longitude`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `login` (`login`);

--
-- Индексы таблицы `user_achievements`
--
ALTER TABLE `user_achievements`
  ADD PRIMARY KEY (`user_id`,`achievement_id`),
  ADD KEY `achievement_id` (`achievement_id`);

--
-- Индексы таблицы `user_photos`
--
ALTER TABLE `user_photos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_user_photos_user` (`user_id`),
  ADD KEY `idx_user_photos_point` (`point_id`),
  ADD KEY `idx_user_photos_created` (`created_at`);

--
-- Индексы таблицы `user_point_progress`
--
ALTER TABLE `user_point_progress`
  ADD PRIMARY KEY (`user_id`,`point_id`),
  ADD KEY `point_id` (`point_id`),
  ADD KEY `idx_user_point_progress_visited` (`visited`);

--
-- Индексы таблицы `user_quest_progress`
--
ALTER TABLE `user_quest_progress`
  ADD PRIMARY KEY (`user_id`,`quest_id`),
  ADD KEY `quest_id` (`quest_id`),
  ADD KEY `idx_user_quest_progress_selected` (`is_selected`),
  ADD KEY `idx_user_quest_progress_enabled` (`is_enabled`);

--
-- Индексы таблицы `user_sessions`
--
ALTER TABLE `user_sessions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `idx_user_sessions_token` (`token`),
  ADD KEY `idx_user_sessions_expires` (`expires_at`);

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `quest_points`
--
ALTER TABLE `quest_points`
  ADD CONSTRAINT `quest_points_ibfk_1` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `user_achievements`
--
ALTER TABLE `user_achievements`
  ADD CONSTRAINT `user_achievements_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `user_achievements_ibfk_2` FOREIGN KEY (`achievement_id`) REFERENCES `achievements` (`id`) ON DELETE CASCADE;

--
-- Ограничения внешнего ключа таблицы `user_photos`
--
ALTER TABLE `user_photos`
  ADD CONSTRAINT `user_photos_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_photos_ibfk_2` FOREIGN KEY (`point_id`) REFERENCES `quest_points` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `user_point_progress`
--
ALTER TABLE `user_point_progress`
  ADD CONSTRAINT `user_point_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_point_progress_ibfk_2` FOREIGN KEY (`point_id`) REFERENCES `quest_points` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `user_quest_progress`
--
ALTER TABLE `user_quest_progress`
  ADD CONSTRAINT `user_quest_progress_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `user_quest_progress_ibfk_2` FOREIGN KEY (`quest_id`) REFERENCES `quests` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Ограничения внешнего ключа таблицы `user_sessions`
--
ALTER TABLE `user_sessions`
  ADD CONSTRAINT `user_sessions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
