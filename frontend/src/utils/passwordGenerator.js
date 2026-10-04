const UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
const LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
const NUMBERS = "0123456789";
const SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";

function getSecureRandomIndex(max) {
    const randomValues = new Uint32Array(1);
    crypto.getRandomValues(randomValues);

    return randomValues[0] % max;
}

function getRandomCharacter(characters) {
    return characters[
        getSecureRandomIndex(characters.length)
    ];
}

function shuffleSecurely(array) {
    for (let i = array.length - 1; i > 0; i--) {
        const randomIndex = getSecureRandomIndex(i + 1);

        [array[i], array[randomIndex]] = [
            array[randomIndex],
            array[i],
        ];
    }

    return array;
}

export function generatePassword({
    length,
    useUppercase,
    useLowercase,
    useNumbers,
    useSpecial,
}) {
    let characterPool = "";

    const selectedCharacters = [];

    if (useUppercase) {
        characterPool += UPPERCASE;
        selectedCharacters.push(
            getRandomCharacter(UPPERCASE)
        );
    }

    if (useLowercase) {
        characterPool += LOWERCASE;
        selectedCharacters.push(
            getRandomCharacter(LOWERCASE)
        );
    }

    if (useNumbers) {
        characterPool += NUMBERS;
        selectedCharacters.push(
            getRandomCharacter(NUMBERS)
        );
    }

    if (useSpecial) {
        characterPool += SPECIAL;
        selectedCharacters.push(
            getRandomCharacter(SPECIAL)
        );
    }

    if (characterPool.length === 0) {
        throw new Error(
            "Select at least one character type."
        );
    }

    if (length < selectedCharacters.length) {
        throw new Error(
            `Password length must be at least ${selectedCharacters.length}.`
        );
    }

    const passwordCharacters = [
        ...selectedCharacters,
    ];

    while (passwordCharacters.length < length) {
        passwordCharacters.push(
            getRandomCharacter(characterPool)
        );
    }

    return shuffleSecurely(
        passwordCharacters
    ).join("");
}