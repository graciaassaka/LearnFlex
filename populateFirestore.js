const admin = require('firebase-admin');

// Initialize Firebase Admin SDK for Firestore Emulator
process.env.FIRESTORE_EMULATOR_HOST = 'localhost:8080'; // Ensure the emulator is running on this default port
admin.initializeApp({
    projectId: 'learnflexkmp', // Replace with your Firebase project ID
});

const db = admin.firestore();

async function addMockData() {
    const profileRef = db.collection('profiles').doc('YJ2m4rjDEEhwUQXg17n0V7hAH8ap');
    await profileRef.set({
        id: 'YJ2m4rjDEEhwUQXg17n0V7hAH8ap',
        username: 'JohnDoe',
        email: 'john.doe@example.com',
        photo_url: 'https://example.com/photo.jpg',
        preferences: {darkMode: true, notifications: true},
        learning_style: 'visual',
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const curriculumRef = profileRef.collection('curricula').doc('curriculum1');
    await curriculumRef.set({
        id: 'curriculum1',
        image_url: 'https://example.com/curriculum.jpg',
        syllabus: 'Introduction to Firestore',
        description: 'Learn Firestore fundamentals.',
        status: 'Unfinished',
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const curriculumRef2 = profileRef.collection('curricula').doc('curriculum2');
    await curriculumRef2.set({
        id: 'curriculum2',
        image_url: 'https://example.com/curriculum.jpg',
        syllabus: 'Introduction to Firebase',
        description: 'Learn Firebase fundamentals.',
        status: 'Unfinished',
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const moduleRef = curriculumRef.collection('modules').doc('module1');
    await moduleRef.set({
        id: 'module1',
        image_url: 'https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg',
        title: 'Firestore Basics',
        description: 'Learn how to structure data in Firestore.',
        index: 1,
        quiz_score: 8,
        quiz_score_max: 10,
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const moduleRef2 = curriculumRef.collection('modules').doc('module2');
    await moduleRef2.set({
        id: 'module2',
        image_url: 'https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg',
        title: 'Firebase Basics',
        description: 'Learn how to use Firebase services.',
        index: 2,
        quiz_score: 8,
        quiz_score_max: 10,
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const moduleRef3 = curriculumRef2.collection('modules').doc('module3');
    await moduleRef3.set({
        id: 'module3',
        image_url: 'https://universemagazine.com/wp-content/uploads/2022/08/zm4nfgq29yi91-1536x1536-1.jpg',
        title: 'Firebase Authentication',
        description: 'Learn how to authenticate users with Firebase.',
        index: 1,
        quiz_score: 8,
        quiz_score_max: 10,
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const lessonRef = moduleRef.collection('lessons').doc('lesson1');
    await lessonRef.set({
        id: 'lesson1',
        image_url: 'https://example.com/lesson.jpg',
        title: 'Introduction to Firestore',
        description: 'Learn the basics of Firestore and its features.',
        index: 1,
        quiz_score: 7,
        quiz_score_max: 10,
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const sectionRefInLesson = lessonRef.collection('sections').doc('section1');
    await sectionRefInLesson.set({
        id: 'section1',
        image_url: 'https://example.com/section.jpg',
        index: 1,
        title: 'Getting Started',
        description: 'Introduction to Firestore.',
        content: 'Firestore is a NoSQL database...',
        quiz_score: 5,
        quiz_score_max: 5,
        created_at: 1672531200000,
        last_updated: 1672531200000,
    });

    const endDate = Date.now();
    const oneWeek = 7 * 24 * 60 * 60 * 1000;
    const startDate = endDate - oneWeek;

    const sessionsPerDay = 2;
    const totalDays = 7;

    function getRandomInt(min, max) {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    const batch = db.batch();

    let sessionNumber = 1;

    for (let day = 0; day < totalDays; day++) {
        for (let s = 0; s < sessionsPerDay; s++) {
            const dayOffset = day * 24 * 60 * 60 * 1000;
            const dayStart = endDate - dayOffset - (24 * 60 * 60 * 1000) + (day === 0 ? 0 : 0);
            const randomTimeWithinDay = getRandomInt(0, 24 * 60 * 60 * 1000 - 1);
            const sessionStartTime = startDate + dayOffset + randomTimeWithinDay;

            if (sessionStartTime > endDate) continue;

            const sessionDurationMinutes = getRandomInt(15, 120);
            const sessionDuration = sessionDurationMinutes * 60 * 1000;
            const sessionEndTime = sessionStartTime + sessionDuration;

            const finalEndTime = sessionEndTime > endDate ? endDate : sessionEndTime;

            const sessionRef = profileRef.collection('sessions').doc(`session${sessionNumber}`);
            batch.set(sessionRef, {
                id: `session${sessionNumber}`,
                end_time: finalEndTime,
                created_at: sessionStartTime,
                last_updated: finalEndTime,
            });

            sessionNumber++;
        }
    }

    await batch.commit();
    console.log('Mock data added successfully via batch!');
}

addMockData()
    .catch((error) => console.error('Error adding mock data:', error));
