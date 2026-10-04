export function analyzePasswordStrength(password) {
    if (!password) {
        return {
            score: 0,
            strength: "Very Weak",
            suggestions: [
                "Enter a password to analyze its strength."
            ],
        };
    }

    let score = 0;
    const suggestions = [];

    const length = password.length;

    const hasLowercase = /[a-z]/.test(password);
    const hasUppercase = /[A-Z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecial = /[^A-Za-z0-9]/.test(password);

    // Password length
    if (length >= 8) {
        score += 1;
    }

    if (length >= 12) {
        score += 1;
    }

    if (length >= 16) {
        score += 1;
    }

    // Character variety
    if (hasLowercase) {
        score += 1;
    }

    if (hasUppercase) {
        score += 1;
    }

    if (hasNumber) {
        score += 1;
    }

    if (hasSpecial) {
        score += 1;
    }

    // Suggestions
    if (length < 8) {
        suggestions.push(
            "Use at least 8 characters."
        );
    } else if (length < 12) {
        suggestions.push(
            "Consider using at least 12 characters."
        );
    }

    if (!hasLowercase) {
        suggestions.push(
            "Add lowercase letters."
        );
    }

    if (!hasUppercase) {
        suggestions.push(
            "Add uppercase letters."
        );
    }

    if (!hasNumber) {
        suggestions.push(
            "Add numbers."
        );
    }

    if (!hasSpecial) {
        suggestions.push(
            "Add special characters."
        );
    }

    let strength;

    if (score <= 2) {
        strength = "Weak";
    } else if (score <= 4) {
        strength = "Medium";
    } else if (score <= 6) {
        strength = "Strong";
    } else {
        strength = "Very Strong";
    }

    return {
        score,
        strength,
        suggestions,
        hasLowercase,
        hasUppercase,
        hasNumber,
        hasSpecial,
        length,
    };
}