const API_BASE = '/api';  // same origin

const teamFormDiv = document.getElementById('teamFormDiv');
const teamForm = document.getElementById('teamForm');
const tasksDiv = document.getElementById('tasksDiv');
const taskListDiv = document.getElementById('taskList');

function setCookie(name, value, days = 7) {
    const expires = new Date(Date.now() + days*24*60*60*1000).toUTCString();
    document.cookie = `${name}=${value}; expires=${expires}; path=/`;
}

function getCookie(name) {
    const match = document.cookie.match(new RegExp('(^| )' + name + '=([^;]+)'));
    if (match) return match[2];
    return null;
}

async function createTeam(name, password) {
    const resp = await fetch(`${API_BASE}/team`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, password}),
    });
    if (!resp.ok) throw new Error('Failed to create team');
    return resp.json();
}

async function fetchTasks(teamId) {
    const resp = await fetch(`${API_BASE}/task/team/${teamId}`);
    if (!resp.ok) throw new Error('Failed to fetch tasks');
    return resp.json();
}
async function submitGuess(taskId, teamId, guess, form, locationSpan, riddleElement, description, task, taskDiv) {
    const resp = await fetch(`${API_BASE}/task/${taskId}/team/${teamId}/${guess}`, {
        method: 'GET',
        headers: { 'Content-Type': 'application/json' }
    });
    const result = await resp.json();

    if (result.correct) {
        locationSpan.textContent = guess;
        riddleElement.textContent = description;
        form.querySelector('input').disabled = true;
        form.querySelector('button').disabled = true;
        form.classList.add('completed');

        if (task.hasSubtask) {
            console.log(task.completed + " " + task.hasSubtask + " " + !task.subtaskComplete + " elif 1")

            const startForm = document.createElement('form');
            const startButton = document.createElement('button');
            startButton.type = 'submit';
            startButton.textContent = 'Start';
            startForm.appendChild(startButton);

            const currentTask = task;

            console.log(currentTask);

            startForm.addEventListener('submit', e => {
                e.preventDefault();
                const completeForm = document.createElement('form');
                const completeButton = document.createElement('button');
                completeButton.type = 'submit';
                completeButton.textContent = 'Start';
                completeForm.appendChild(completeButton);

                completeForm.addEventListener('submit', ce => {
                    ce.preventDefault();
                    if (confirm("Start subtask?")) {
                        completeButton.remove();
                        startButton.remove();
                        showSubtask(riddleElement, currentTask, taskDiv);
                    }
                });

                taskDiv.replaceChild(completeForm, startForm);
            });

            taskDiv.appendChild(startForm);

        } else {
            // No subtask, immediately show complete button
            const completeForm = document.createElement('form');
            const completeButton = document.createElement('button');
            completeButton.type = 'submit';
            completeButton.textContent = 'Complete';
            completeForm.appendChild(completeButton);

            completeForm.addEventListener('submit', e => {
                e.preventDefault();
                if (confirm("Mark this challenge as completed?")) {

                    submitCompletion(task.id, taskDiv);
                }
            });

            taskDiv.appendChild(completeForm);
        }

    } else {
        const input = form.querySelector('input[name="guess"]');
        input.classList.add('shake');
        input.addEventListener('animationend', () => {
            input.classList.remove('shake');
        }, { once: true });
    }

    form.reset();
}

function showSubtask(riddleElement, task, taskDiv) {

    console.log("Show subtask: " + task)

    riddleElement.textContent = task.subtaskDescription;
    // No subtask, immediately show complete button
    const completeForm = document.createElement('form');
    const completeButton = document.createElement('button');
    completeButton.type = 'submit';
    completeButton.textContent = 'Complete';
    completeForm.appendChild(completeButton);

    completeForm.addEventListener('submit', e => {
        e.preventDefault();
        if (confirm("Mark this challenge as completed?")) {
            submitCompletion(task.id, taskDiv);
        }
    });

    taskDiv.appendChild(completeForm);
}

function showTasks(tasks, teamId) {
    taskListDiv.innerHTML = '';
    tasks.forEach(task => {
        const taskDiv = document.createElement('div');
        taskDiv.className = 'task';

        const title = document.createElement('h3');
        title.textContent = task.name;

        const content = document.createElement('p');
        content.textContent = task.location !== "Hidden" ? task.description : task.riddle;

        const locationSpan = document.createElement('span');
        locationSpan.textContent = task.location === "Hidden" ? "Hidden" : task.location;

        taskDiv.appendChild(title);
        taskDiv.appendChild(document.createTextNode(task.location !== "Hidden" ? 'Challenge: ' : 'Riddle: '));
        taskDiv.appendChild(content);
        taskDiv.appendChild(document.createTextNode('Location: '));
        taskDiv.appendChild(locationSpan);
        taskDiv.appendChild(document.createElement('br'));

        if (!task.completed && task.location === "Hidden") {

            console.log(!task.completed + " " + task.location + " first if")
            const form = document.createElement('form');
            form.innerHTML = `
                <input type="text" name="guess" placeholder="Your guess" required />
                <button type="submit">Guess</button>
            `;
            form.addEventListener('submit', e => {
                e.preventDefault();
                const guess = form.guess.value.trim();
                if (!guess) return;
                submitGuess(task.id, teamId, guess, form, locationSpan, content, task.description, task, taskDiv);

            });
            taskDiv.appendChild(form);

        } else if (task.completed && (!task.hasSubtask || task.subtaskComplete)) {
            console.log(task.completed + " " + !task.hasSubtask + " " + task.subtaskComplete + " elif 2")

            const completeForm = document.createElement('form');
            completeForm.innerHTML = `<button type="submit">Complete</button>`;
            completeForm.addEventListener('submit', e => {
                e.preventDefault();
                if (confirm("Mark this challenge as completed?")) {
                    submitCompletion(task.id, taskDiv);
                }
            });
            taskDiv.appendChild(completeForm);
        } else if (task.completed && task.hasSubtask && !task.subtaskComplete) {

            const startForm = document.createElement('form');
            const startButton = document.createElement('button');
            startButton.type = 'submit';
            startButton.textContent = 'Start';
            startForm.appendChild(startButton);

            const currentTask = task;
            console.log(currentTask + " task")

            startForm.addEventListener('submit', e => {
                e.preventDefault();
                // Replace with "Complete" button
                const completeForm = document.createElement('form');
                const completeButton = document.createElement('button');
                completeButton.type = 'submit';
                completeButton.textContent = 'Start';
                completeForm.appendChild(completeButton);

                completeForm.addEventListener('submit', ce => {
                    ce.preventDefault();
                    if (confirm("Start subtask?")) {
                        showSubtask(content, currentTask, taskDiv);

                        completeButton.remove();
                        startButton.remove();
                    }
                });

                taskDiv.replaceChild(completeForm, startForm);
            });

            taskDiv.appendChild(startForm);

        }
        console.log(task.completed + " " + task.hasSubtask + " " + task.subtaskComplete + JSON.stringify(task) +  " out 1")

        taskListDiv.appendChild(taskDiv);
    });
}

async function submitCompletion(taskId, taskCard) {
    const teamId = getCookie('teamId');
    if (!teamId) return;

    console.log("animation")

    taskCard.classList.add('slide-out-right');
    taskCard.addEventListener('animationend', () => {
        taskCard.remove();
    }, { once: true });

}

// Admin stuff
function isAdminLoggedIn() {
    return !!localStorage.getItem('adminToken');
}

// Show admin toggle only if logged in
const adminToggle = document.getElementById('adminToggle');
adminToggle.addEventListener('click', () => {
    if (!isAdminLoggedIn()) {
        alert('Please log in as admin first!');
        window.location.href = '/admin-login.html';
        return;
    }

    const panel = document.getElementById('adminTaskDiv');
    panel.style.display = panel.style.display === 'none' ? 'block' : 'none';

    if (panel.style.display === 'block') {
        loadAdminPanel();
    }
});

// API calls
async function fetchAllTasks(teamId) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    const resp = await fetch(`${API_BASE}/task/team/${teamId}`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return await resp.json();
}

async function fetchAllTeams() {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    const resp = await fetch(`${API_BASE}/team`, {
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    return await resp.json();
}

async function deleteTask(id) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    if (!confirm("Are you sure you want to delete this task?")) return;
    await fetch(`${API_BASE}/task/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    await loadAdminPanel();
}

async function deleteTeam(id) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    if (!confirm("Are you sure you want to delete this team?")) return;
    await fetch(`${API_BASE}/team/${id}`, {
        method: 'DELETE',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    });
    await loadAdminPanel();
}

async function createTaskFromAdmin(data) {
    const token = localStorage.getItem('adminToken');
    if (!token) {
        return;
    }
    await fetch(`${API_BASE}/task`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(data)
    });
    await loadAdminPanel();
}

// Load admin panel content
async function loadAdminPanel() {
    const tasks = await fetchAllTasks(getCookie("teamId"));
    const teams = await fetchAllTeams();

    const adminTasksList = document.getElementById('adminTaskList');
    adminTasksList.innerHTML = '';

    const adminTasksDiv = document.getElementById('adminTaskDiv');
    // TASKS
    const taskTitle = document.createElement('h2');
    taskTitle.textContent = 'Tasks';
    adminTasksDiv.appendChild(taskTitle);

    tasks.forEach(task => {
        const div = document.createElement('div');
        div.className = 'task';

        // Create and append title
        const title = document.createElement('h3');
        title.textContent = task.name;
        div.appendChild(title);

        // Create and append riddle paragraph
        const riddle = document.createElement('p');
        riddle.textContent = task.riddle;
        div.appendChild(riddle);

        // Create and append location span
        const location = document.createElement('span');
        location.textContent = task.location;
        div.appendChild(location);

        div.appendChild(document.createElement('br'));

        // Create and append delete button
        const btn = document.createElement('button');
        btn.textContent = 'Delete';
        btn.addEventListener('click', () => deleteTask(task.id));
        div.appendChild(btn);

        adminTasksDiv.appendChild(div);
    });

    // TEAMS
    const teamTitle = document.createElement('h2');
    teamTitle.textContent = 'Teams';
    adminTasksDiv.appendChild(teamTitle);

    teams.forEach(team => {
        const div = document.createElement('div');
        div.className = 'task';

        const strong = document.createElement('strong');
        strong.textContent = team.name;

        const idText = document.createElement('div');
        idText.textContent = `(ID: ${team.id})`;

        const btn = document.createElement('button');
        btn.textContent = 'Delete';
        btn.addEventListener('click', () => deleteTeam(team.id));

        div.appendChild(strong);
        div.appendChild(document.createElement('br'));
        div.appendChild(idText);
        div.appendChild(document.createElement('br'));
        div.appendChild(btn);

        adminTasksDiv.appendChild(div);
    });

    adminTasksDiv.style.display = 'block';
}

document.getElementById('hasSubtask').addEventListener('change', (e) => {
    document.getElementById('subtaskFields').style.display = e.target.checked ? 'block' : 'none';
});

document.getElementById('adminTaskForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const form = e.target;

    const data = {
        name: form.name.value,
        riddle: form.riddle.value,
        description: form.description.value,
        location: form.location.value,
        completed: false,
        hasSubtask: false,
        subtaskComplete: false,
    };

    if (form.hasSubtask.checked) {
        data.hasSubtask = true;
        data.subtaskDescription = form.subtaskDescription.value

    }

    console.log(JSON.stringify(data))

    await createTaskFromAdmin(data);
    form.reset();
    document.getElementById('subtaskFields').style.display = 'none';  // Hide again after submit
});

function setActiveStyleSheet(title) {
    const links = document.querySelectorAll('link[rel~="stylesheet"], link[rel="alternate stylesheet"]');
    links.forEach(link => {

        link.disabled = link.title !== title;
    });
    localStorage.setItem('selectedTheme', title);
}

// On load, set theme and listen for changes
window.addEventListener('DOMContentLoaded', () => {
    const savedTheme = localStorage.getItem('selectedTheme') || 'Base';
    setActiveStyleSheet(savedTheme);
    document.getElementById('themeSelect').value = savedTheme;

    document.getElementById('themeSelect').addEventListener('change', function () {
        setActiveStyleSheet(this.value);
    });
});

const themeSwitcher = document.getElementById('themeSwitcher');
const toggleBtn = document.getElementById('themeToggle');

toggleBtn.addEventListener('click', () => {
    themeSwitcher.classList.toggle('open');
});

// On page load check for teamId cookie or show form
async function init() {
    let teamId = getCookie('teamId');

    if (!teamId) {
        teamFormDiv.style.display = 'block';
        tasksDiv.style.display = 'none';

        teamForm.onsubmit = async e => {
            e.preventDefault();
            const name = document.getElementById('teamName').value.trim();
            if (!name) return alert('Please enter a team name');
            const pass = document.getElementById('password').value.trim();
            if (!pass) return alert("Please enter a password");

            try {
                const team = await createTeam(name, pass);
                setCookie('teamId', team.id);
                teamId = team.id;
                teamFormDiv.style.display = 'none';
                tasksDiv.style.display = 'block';

                const tasks = await fetchTasks(teamId);
                showTasks(tasks, teamId);
            } catch (err) {
                alert('Failed to create team: ' + err.message);
            }
        };

    } else {
        // Already have teamId cookie, show tasks directly
        try {
            const tasks = await fetchTasks(teamId);
            teamFormDiv.style.display = 'none';
            tasksDiv.style.display = 'block';
            showTasks(tasks, teamId);
        } catch {
            // Invalid teamId in cookie: reset and show form
            setCookie('teamId', '', -1);
            teamFormDiv.style.display = 'block';
            tasksDiv.style.display = 'none';
        }
    }

    if (isAdminLoggedIn()) {
        document.getElementById('adminToggle').style.display = 'block';
    }
}



window.onload = init;